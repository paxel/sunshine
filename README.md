# Sunshine

## Introductional Overview

Sunshine is a code generator and a serialization API like capn proto, flatbuffers, avro etc.
It uses a schema to generate (Java) code from it.
This code can be used to create, read and write (data) objects.
These objects can be stored, transmitted, read and modified.

Sunshine tries to have a streamable, well defined data format.
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

### Custom values (Data Objects)

It is possible to define custom values, by combining BasicTypes into data objects.
* **Object**: a complex structure containing a high (but limited) amount of BasicTypes, Lists and other data Objects.

An Object is composed by three different parts:

* **Constant<BasicType>**: a value that is available in the generated Code, but not in the serialized data.
A Constant Value is defined in the schema and can only be modified by changing the schema.
The number of defined constants is unlimited (except Java compiler limitations)
* **Fixed sized values**: All values that are of a fixed size, are combined into a fixed sized byte array block (Static Block).
Each value has a well defined position inside this block.
The number of values is limited Only by the maximum size of a frame (which is bigger than the usual memory of a PC 2021)
* **Dynamic sized values**: All values that are of a dynamic size are added as a stream of values after the Static Block.
Depending on the Type, they are either embedded into the defining object or just a reference to another object.

The number of dynamic values (members) of a single object is limited to 252.
* 0x00 being a NOP Value
* 0x01 to 0xfc for the dynamic Values
* 0xfd reserved for the static block
* 0xfe the end marker
* 0xff a reserved value for future extensions)

Objects can also be collected:

* **Array\<Object>**: a Fixed List of Objects
* **List\<Object>**: a variable List of Objects
* **Optional\<Object>**: a special List of Objects, that is size 0 or 1

**NOTE:** The difference between a Dynamic Value and an Optional Value is following:
An Optional Value has a ```public boolean hasNameOfValue() {}``` method and in case the value is not present, will throw an Exception if you access the getter.
A Dynamic Value that is not present will be null, 0, 0.0 or an empty List, which can make the default values indistinguishable from not present.
But that might also be desired in some cases.

**NOTE:** Currently it is not planned to allow for preset values other than constants.

### Schema

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

**NOTE:** reordering members in the Schema will **always** lead to incompatibility between versions.
Changing types can lead to incompatibility, if the new type has a different size.
You can rename the value names however without any problems.
It is recommended to rename replaced values to deprecatedValueName and add a new value at the end of the Object.

```
Object Tyre {
  Bool deprecatedTubeless
  UInt16 type   
}
```
This change in Tyre would be compatible.
Old versions of Tyre would not set type, but the new version could still support the old version by reading the deprecated value.
New versions could write the type; but old versions could of course not access the value.


### Serialized Data


#### Framing

The serialized data has no frame itself.
But it is recommended for easy and fast parsing to use the provided sun framing.
The frame consists of the magic number 0x73756e (ascii for 'sun') and a length, that is either 4 bytes (if the most significant bit is 0) or 8 bytes (if the most significant bit is 1)

73 75 6E  00 00 00 00                  | sun ..
73 75 6E  80 00 00 00  00 00 00 00     | sun .... ....

73 75 6E  00 04 00 00  xx xx xx xx     | sun ..xx xx
73 75 6E  80 00 00 00  00 00 00 01  xx | sun .... .... x

#### Message format

Each serialization results in one Message that contains one or more Object Blocks and a End of Message flag (EOM).
Each Object starts with an Object Header and ends with an End of Object flag (EOO) which is just two EOM flags.
The different values in the Object are defined by typed values (some of them are Type Lenght Values, but not all).

##### Object format

The object header consists of the magic number obj 0x6f626a (ascii for obj) and a two byte type ID and a four byte instance ID.
The first instance of each message will be 1.
The ID depends on the position of the object in the schema. starting with 1.

```
6F 62 6A  00 01  00 00 00 01   | obj .. ....
```

##### Static Block Value (Type 0xfd)

_Format: TLV (1byte, 1byte, x bytes)_

Normally the header is immediately followed by the Static Block, if available.
It consists of the Object Value type 0xfd, a length and the actual block bytes.
The length is defined by 1 byte.
If the static block is bigger than the possible 256 bytes, another static block is added until all bytes are handled.

```
fd 04 xx xx xx xx                   
--
253                                       type fd = static block
   --
   4                                    length 4 = 4 bytes following containing the statis size Data
```

The purpose of the static block is to have the least overhead for serializing fixed size data.
The code generator scans the defined values of an Object in the Schema, calculates the static block size and defines for each value its position in the data:

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

Because of this restricted representation, no dynamic sized Datatype can be in the Static Block:
* No String (only FixString)
* No Binary (only FixBinary)
* No List<> (only Array<> of the allowed types!)

**Objects:** Every Object value is added as an object instance ID (4 bytes) and is therefore of a fixed sized and part of the Static Block.
The actual data of the member is stored somewhere after the current object in its own object instance.
This allows to separate the data blocks instead of embedding them in each other, which simplifies memory management and modification of existing data.
A List of objects is just a number of reference IDs.
If they are NOT set, their value will be 0x00000000 which is a restricted value for unset Objects.

##### Dynamic Values (0x01 - 0xfc)

_Format: TLV (1byte, 1byte, x bytes)_

The dynamic sized values are formatted like the following:

1 byte ID
1 byte size Definition
x byte the inline data or the reference to a followup object

The ID goes from 0x01 to 0xfe.
0x00 being reserved for NOP, 0xfd for static Block, 0xfe for End of Object (0xfefe being End of Message) and 0xff being reserved for future changes.
The ID is the position of the Dynamic Value Definition in the Schema starting with 1.

The 1 byte size definition is sufficient because for data, that is longer than 256 bytes, you just repeat the value.

```
01 ff [256 bytes of data]
01 0a [ 10 bytes of data]
```

###### Data Format Examples

Binary is represented as bytes
```
01  04  11 22 33 44 | . . ....
```

String is represented as UTF8 bytes
```
01  04  65 66 66 65 | . . ABBA
```


List is represented as bytes that have repeated values depending on the type

A list of 4 Bytes,Bool or UBytes looks like this
```
01  04  00  00  00  01 | . . . . . .
```

A list of 2 Int16 or UInt16 looks like this
```
01  04  0000  0001 | . . .. ..
```

A list of 1 Int32, Float32 or UInt32 looks like this
```
01  04  0000 0001 | . . ....
```

A list of 2 Int64, Float64 or UInt64 looks like this
```
01  10  0000 0001 9911 1111  9433 3333 0000 0001
```

A list of 4 Objects looks like this
```
01  10  0000 0001  9911 1111  9933 3333  0000 0002
```

A list of Binary and String are only represented as repeated values.
A list of different sized Binary (and also String) looks like this
```
01  10  00 00 00 01 99 11 11 11 99 33 33 33 00 00 00 01
01  11  00 00 00 01 99 11 11 11 99 33 33 33 00 00 00 01 12
01  08  00 00 00 01 99 11 11 11
01  01  00
```

**NOTE:** all other (supported) values can of course also be represented as repeated values.

**NOTE:** There is currently no List of Lists.
If you urgently need something like this, please define an object containing a List and create a List of this Objects.  


##### NOP Value (0x00)

_Format: T (1byte)_

There is a special NOP data format (0x00) that is used to tell the parser to ignore this byte.
The purpose of this is to delete dynamic data without having to copy all following bytes to the deleted position.
NOPs can be added in any number to the message.

**NOTE:** The object Builder will try to reuse NOP areas if possible.

##### EOO Value (0xfe)

_Format: T (1byte)_

This Type defines the end of an object.
It is either followed by a new object header or another EOO marking the end of the message (EOM).


#### Small example of a fully framed message

```
73 75 6E 00 00  00 15         | frame defining 21 bytes message (optional)
6F 62 6A 00 01  00 00 00 01   | object type 1 instance 1
FD 04 00 00 00  77            | static block with 4 bytes value 0x00000077
01 01 65                      | Dynamic Value ID 1 size 1 value 0x65
00                            | NOP (0x00). A NOP is a filler that tells the parser to ignore this byte and continue parsing with the next
fe                            | End of Object
fe                            | End of Message (because repeated)
```


## Runtime

The Runtime library is needed for using the generated sunshine code in your project.
It also provides some utils to read and write the serialized data.
It provides also an API if you need to extend parts for your environment.

You find the runtime code at [github](https://github.com/paxel/sunshine)

### RandomAccessMemory


The Runtime has an abstraction layer for accessing the memory containing the serialized data.
The simplest interface only provides bytes and byte ranges at a defined position in the memory.

#### ReadOnlyRandomAccessMemory (RO-RAM)

```Java
 public byte getByteAt(long index);
 public byte[] getBytesAt(long index);
 public void writeBytesIntoArray(long index, int length, byte[] target, int offsetInArray);
 public void writeBytesIntoByteBuffer(long index, int length, ByteBuffer target);
 public <T> void writeBytesIntoGeneric(long index, int length, T target);
 public long size();
```

More advanced implementations also provide the retrieval of the BasicTypes directly from the memory without converting it to byte arrays.

```Java
 public long getUInt32At(long index);
```

If the RandomAccessMemory does not provide these methods, it wraps the RAM with an implementation that provies these methods.

#### ReadWriteRandomAccessMemory (RW-RAM)

This interface in its simplest implementation allows to put byte and byte ranges at a defined position in the memory.

```Java
 public void getByteAt(long index);
 public void writeBytesIntoRam(long index, byte[] source, int offsetInArray, int length);
 public void writeBytesIntoRam(long index, ByteBuffer source);
 public <T> void writeBytesIntoRam(long index, T target);
 public long size();
```

In addition, the RW-RAM also needs to be able to extend the currently used memory in case not enough memory is allocated.
This allocation looks like this:

```Java
 public long allocate(long size);
```
The parameter is the amount of data that is allocated and the return value is the amount of bytes the Memory is now in use.

More advanced implementations also provide the writing of the BasicTypes directly to the memory without converting it to byte arrays.

```Java
 public void writeUInt32IntoRam(long index, long value);
```

**NOTE:** each allocation can be somewhere in the memory and will be put behind each other when serialized.

Another requirement for the RandomAccessMemory is that it needs to be able to create lightweigth slices of itself.
A slice of RAM is a new Instance of RandomAccessMemory that uses another RandomAccessMemory as Memory but with its own index (starting at 0) and appending its own Memory when new memory is allocated.

The Runtime provides different Wrapper and a default implementation that can use ByteBuffers with internal and external memory.

```
[ReadOnlyRandomAccessMemory]    [RichReadOnlyRandomAccessMemory]
   |                                 /
   |                               /  
[ReadWriteRandomAccessMemory]    /   [RichReadWriteRandomAccessMemory]
    |                          /    /
    |                        /    /
   [ByteBufferRandomAccessMemory]
```

As there are some special memory implementations already in existence that do stuff differently than the ByteBuffers of the JDK (e.g. the ByteBuf of Netty) These interfaces and wrappers were introduced, to allow support for those.

**NOTE:** the exact definition of the interfaces is WIP

### MessageData

This is the Meta data of a whole message.
It has at least one ObjectData for the root element.
It also has the compiled SchemaData.

MessageData is generated in two ways: Either by reading a serialized Message, or by creating a Message with the Builder.
It has no RandomAccessMemory itself

### ObjectData

For each Object in a Message an ObjectData instance exists.

The ObjectData contains:
* Information about the positions and existences of NOP, StaticBlock and Dynamic Values.
* The RandomAccessMemory of the Object (with all required Wrappers)
* The MessageData to access embedded ObjectData
* The SchemaData

The generated code will mainly interact with the ObjectData instance to get/set the user data to/from the RandomAccessMemory.

### SchemaData

This is generated code that contains the map between object ID and the Java Class representing the Object.
It might also hold more data if required.

### MessageHandlerRegistry

The MessageHandlerRegistry is a registry where for each expected root Object type a method is added to be called by the MessageParser to initialize and handle the root object of a message.
It is only used when deserializing existing messages.

```java
 public <T> void register(Class<T> key, Consumer<T> messageHandler, Supplier<T> instanceProvider );
 public <T> void register(Class<T> key, Consumer<T> messageHandler);
 public void fallback( Consumer<Object> messageHandler);
```

The key defines the Object Type (one of the objects in the Schema) that will be handled.
The method (messageHandler) defines what is called, if the received Message is actual of the Objects type.
The optional Supplier can be used to reuse a instance of T to reduce garbage collection.

All Object Types that are not registered, will either be ignored or if available given to the fallback consumer.

### FrameReader

This is used to read framed sunshine messages.
It parses the frame header and provides the frame size of the next frame.
It can also be used to provide a RandomAccessMemory of the next Frame for various inputs.

### MessageReader

This is used to read sunshine messages.
If the message is Framed, it uses the FrameReader, otherwise it reads the objects until EOM.
Its outcome is a RandomAccessMemory of the next Frame for various inputs.

**NOTE:** To use custom RandomAccessMemory implementations, it is probably required to implement either Frame- and/or MessageReader.

### MessageParser

The MessageParser takes a MessageHandlerRegistry, a SchemaData and a RandomAccessMemory containing a serialized to create a MessageData instance with all ObjectData instances.
Then it initializes the root object and calls the registered Consumer in the MessageHandlerRegistry.

#### Creating MessageData and ObjectData instances

Similar to capn proto and flatbuffers, it does not store data in members, but immediately serializes it into a defined memory portion.
In the same way when deserializing it does not convert the data to valriables, but stores the read data in a format, that wrapper objects can parse and provide the information on demand.

The first Object in a Message is the one referencing all the embedded Objects (if any).
The root Object can be any Object Type in the Schema.

First a MessageData object is created.
Then the first object is parsed:

- at position 0 to 2 it reads the bytes 0x6F 0x62 0x6A.
- at position 3 it reads 2 bytes as an UInt16 which is the message Type.
- at position 5 it reads 4 bytes as an UInt32 that is expected to be 1 for the root Message.
- it creates an ObjectData instance
- it reads through the T and TLV of the object until it finds the EOM.
  * it updates the ObjectData with a RandomAccessMemory slice of the RandomAccessMemory containing all object memory.
  * for all TLVs the positions of the different value IDs.
  * NOP ranges if any
  * adds the ObjectData with its ID in the MessageData.
- it checks if that was the last object, otherwise it reads the next object.

After all Objects have been parsed, The root object is initialized and given to the registered Consumer.



## Notes (to be used later)


So this could be the code for accessing a boolean
```java
public boolean isTubeless(){
  return ram.getBoolean( helper.getStaticBlockStart()  +  1 /*offset in Static Block*/ );
}
```

This could be the code to check if an Optional is available. And a probably not final version of getting a Tyre Member
```java
public boolean hasTyre(){
  return helper.hasId(  3 /*ID for Tyre*/ );
}

public Tyre getTyre(Tyre tyre){
  if (!hasTyre()){ throw new IllegalStateException ("Tyre not available")}
  long pos = helper.getStart( 3 /*ID for Tyre*/);
  Tlv<UInt32> tlv = ram.readUInt32Tlv(pos);
  assert(tlv.type()== 3 /*ID for Tyre*/);
  if (tyre == null){
    tyre = new Tyre();
  }
  return (Tyre) helper.createObject(tlv.value(), tyre.init(ram,helper,tlv.value()));
}

public Tyre getTyre(){
  return getTyre (null);
}
```
