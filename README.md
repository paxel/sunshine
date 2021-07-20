# Sunshine

## Introductional Overview

Sunshine is a code generator and a serialization API like capnp proto, flatbuffers, avro etc.
It uses a schema to generate (Java) code from it.
This code can be used to create, read and write (data) objects.
These objects can be stored, transmitted, read and modified.

Sunshine tries to have a streamable, well defined data format
Porting to different languages should be easy.

It also tries to create very usable code, so that it is usable in production even if it is not meant to be serialized.

... well at some point in the future maybe

## Concepts

### Built in values

Sunshine knows following BasicTypes:

* **Bool**: one bit with values from false to true _(In Java represented by a boolean)_
* **Byte**: one signed byte with values from -128 to 127 _(In Java represented by a byte)_
* **UByte**: one unsigned byte with values from 0 to 255. _(In Java the getter provides a short)_
* **Int16**: two byte signed int with values from -32768 to 32767 _(In Java represented by a short)_
* **UInt16**: two byte unsigned int with values from 0 to 65535 _(In Java the getter provides an int)_
* **Int32**: four byte signed int with values from -2147483648 to 2147483647 _(In Java represented by an int)_
* **UInt32**: four byte unsigned int with values from 0 to 4294967295 _(In Java the getter provides a long)_
* **Int64**: eight byte signed int with values from -9223372036854775808 to 9223372036854775807 _(In Java represented by a long)_
* **UInt64**: eight byte unsigned int with values from 0 to 18446744073709551615 _(In Java the getter provides a Ulong object)_
* **Float32**: four byte float with values from 1.4E-45 to 3.4028235E38 _(In Java represented by a float)_
* **Float64**: eight byte float with values from 4.9E-324 to 1.7976931348623157E308 _(In Java represented by a double)_
* **String**: UTF8 String value _(In Java represented by a String)_
* **FixString**: UTF8 String value with a fixed size. filled up with 0x00 if shorter _(In Java represented by a String)_
* **Binary**: an array of bytes _(In Java represented by a RandomAccessMemory)_
* **FixBinary**: an array of bytes with a fixed length and a size field _(In Java represented by a RandomAccessMemory)_

And additionally can collect BasicTypes as:

* **Array\<BasicType>**: a fixed sized List of BasicTypes
* **List\<BasicType>**: a variable sized List of BasicTypes
* **Optional\<BasicType>**: a special List of BasicTypes, that is size 0 or 1

### Custom values

It is possible to define custom values, by combining BasicTypes into data objects.
* **Object**: a complex structure containing a high (but limited) amount of BasicTypes, Lists and other data Objects.

An Object is composed by three different parts:

* **Constant<BasicType>**: a value that is available in the generated Code, but not in the serialized data. A Constant value is defined in the schema and can only be modified by changing the schema. The number of defined constants is unlimited (except Java compiler limitations)
* **Static sized values**: All values that are of a static size, are combined into a fixed sized byte array block. Each value has a well defined position inside this block. The number of values is limited Only by the maximum size of a frame (which is bigger than the usual memory of a PC 2021)
* **Dynamic sized values**: All values that are of a dynamic size are added as a stream of values after the static block. Depending on the Type they are embedded into the object or a reference to another object.

The number of dynamic values (members) of a single object is limited to 252. (0x00 being a NOP value, 0xfd reserved for the static block, 0xfe the end marker and 0xff a reserved value for future extensions)

Objects can also be collected:

* **List\<Object>**: a variable List of Objects
* **Optional\<Object>**: a special List of Objects, that is size 0 or 1

NOTE: There is no Array for Object

#### Static block

The purpose of the static block is to have the least overhead for serializing fixed size data.
The code generator scans the defined values of an Object, calculates the static block size and defines for each value its position in the data:

For example if you have 2 Int16, an Int32 and a 10 chars FixString, the binary data looks like this:

```
02 34 17 04 12 13 14 15 6d 6f 6e 6f 00 00 00 00 00 00
-----
  564                                                 Int16
      -----
       5892                                           Int16
            -----------
            303240213                                 Int32
                        -----------------------------
                        mono                          FixString
```

Because of this restricted representation any dynamic sized Datatype can not be in the Static Block: No Objects (even if it only has a Static Bloc), no String (only FixString), no Binary (only FixBinary) and only Array<> of the allowed types!

#### Dynamic block

Every present dynamic member of an object is added as a type ID (2 bytes) + instance ID (4 bytes).
The actual data of the member is stored after the current object.
This allows to separate the data blocks instead of embedding them in each other, which simplifies memory management and modification of existing data.
For example if you want to delete a member, you just zero out the 6 bytes (all zeros being a valid representation of no data) and skip the now not referenced data of the deleted element when serializing.
As a list is just a number of references

## Schema

The Schema is very basic and has no restriction except:

* **Comments** start with ```//``` or ```#``` and go to the end of the line
* **Blocks** Objects are inside brackets ```{}```
* **Const** the Values are defined after declaration with an equal sign. e.g. ```VALUE = 342```
* **Type Definition** are in ```<>``` e.g. ```Optional<String>```
  * Only List, Array and Optional have Type Definitions
* **Size Definition** are in ```[]``` and after the Type Definition e.g. ```Array<Int16>[4]```
  * Only Array, FixBinary and FixString have Size Definitions
Example Schema

```
Object Car {
  Const String BRAND = "Edison"
  Const UInt16 MANIFACTOR_ID = 1833

  // comments are allowed
  # in different styles

  # The order in the Static block defines the order in the serialized Data

  FixString[10] name # no ; at the end required
  UInt16 horsePower
  Bool electric
  Array<Int16>[4] countryCodes

  # All the optional and dynamic sized data must be outside of the Static Block
  List<Tyre> tyres
  Engine engine
  Optional<String> description
  Optional<Binary> blob
}

Object Tyre {
  Bool tubeless    
}

Object Engine {
  String name
}
```


## Serialized Data

### Framing

The serialized data has no frame itself.
But it is recommended for easy and fast parsing to use the provided sun framing.
The frame consists of the magic number 0x73756e (ascii for 'sun') and a length, that is either 2 bytes (if the most significant bit is 0) or 8 bytes (if the most significant bit is 1)

73 75 6E  00 00                        | sun ..
73 75 6E  80 00 00 00  00 00 00 00     | sun .... ....

73 75 6E  00 04 xx xx  xx xx           | sun ..xx xx
73 75 6E  80 00 00 00  00 00 00 01  xx | sun .... .... x

### Message format
Each serialization results in one Message that consists of one or more object messages and a End of Message flag (EOM)
Each object starts with an object header and ends with an End of Object flag (EOO)
The different values in the object are defined by typed values.

#### Object format

The object header consists of the magic number obj 0x6f626a (ascii for obj) and a two byte type ID and a four byte instance ID.
The first instance of each message will be 1.
The ID depends on the position of the object in the schema. starting with 1.

```
6F 62 6A  00 01  00 00 00 01   | obj .. ....
```

Normally the header is immediately followed by the static block, if available.
It consists of the object value type 1, a length and the actual block bytes.
The length is 1 byte. If the static block is bigger than 256 bytes, another static block is added until all bytes are handled.

```
fd 04 xx xx xx xx                   
--
253                                       type fd = static block
   --
   4                                    length 4 = 4 bytes following containing the statis size Data
```

The dynamic sized values are formatted like the following:

1 byte ID
1 byte size Definition
x byte the inline data or the reference to an followup objects

The ID goes from 0x01 to 0xfe. 0x00 being reserved for NOP, 0xfd for static Block, 0xfe for End of Object (0xfefe being End of Message) and 0xff being reserved for future changes.
The ID is the position of the dynamic value Definition in the Schema starting with 1.

The 1 byte size definition is sufficient because for data, that is longer than 256 bytes, you just repeat the value.

```
01 ff [256 bytes of data]
01 0a [ 10 bytes of data]
```

There is a special NOP data format (0x00) that is used to tell the parser to ignore the data.
The purpose of this is to delete dynamic data without having to copy all following contents.
NOPs can be added in any number to the message.
The serialization will try to reuse NOP areas if possible.

##### Data formats

Binary is represented as bytes
```
01 04 11 22 33 44 | ......
```

String is represented as UTF8 bytes
```
01 04 65 66 66 65 | ..ABBA
```

Object is represented as a reference ID
```
01 04 00 00 00 01 | ......
```

List is represented as bytes that have repeated values depending on the type

A list of 4 Bytes,Bool or UBytes looks like this
```
01 04 00 00 00 01 | ......
```

A list of 2 Int16 or UInt16 looks like this
```
01 04 00 00 00 01 | ......
```

A list of 1 Int32, Float32 or UInt32 looks like this
```
01 04 00 00 00 01 | ......
```

A list of 2 Int64, Float64 or UInt64 looks like this
```
01 10 00 00 00 01 99 11 11 11 99 33 33 33 00 00 00 01
```

A list of 4 Objects looks like this
```
01 10 00 00 00 01 99 11 11 11 99 33 33 33 00 00 00 01
```

A list of Binary and String are only represented as repeated values.
A list of different sized Binary (and also String) looks like this
```
01 10 00 00 00 01 99 11 11 11 99 33 33 33 00 00 00 01
01 11 00 00 00 01 99 11 11 11 99 33 33 33 00 00 00 01 12
01 08 00 00 00 01 99 11 11 11
01 01 00
```

NOTE: all other (supported) values can of course also be represented as repeated values.

NOTE: There is currently no List of Lists.
If you urgently need something like this, please define an object containing a List and create a List of objects.  

NOTE: FixString, Array and FixBinary are only used in static block.
They can not be used in Lists.


Small example of a fully framed message

```
73 75 6E 00 15                        | frame defining 21 bytes message (optional)
6F 62 6A 00 01  00 00 00 01           | object type 1 instance 1
fd 04 00 00 00  77                    | static block with 4 bytes value 0x00000077
01 01 65                              | dynamic value ID 1 size 1 value 0x65
00                                    | NOP (0x00). A NOP is a filler that tells the parser to ignore this byte and continue parsing with the next
fe                                    | End of Object
fe                                    | End of Message (because repeated)
```
