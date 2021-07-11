# Sunshine

## Introductional Overview

Sunshine is a code generator and a serialization API like capnp proto, flatbuffers, avro etc.
It takes a schema and generates code from it.
The code can be used to create, read and write data.
This data can be stored, transmitted, read and modified.

Sunshine tries to have a streamable, well defined data format
Porting to different languages should be easy.

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

* **Array\<BasicType>**: a fixed List of BasicTypes
* **List\<BasicType>**: a variable List of BasicTypes
* **Optional\<BasicType>**: a special List of BasicTypes, that is size 0 or 1

### Custom values

It is possible to define custom values, by combining BasicTypes into Objects.
* **Object**: a complex structure containing any amount of BasicTypes, Lists and other Objects.

An Object consists of three parts:

* **Constant<BasicType>**: a value that is available in genereated Code, but not in the serialized data. a Constant value is defined in the schema and can only be modified by changing the schema.
* **Static{}**: a Block of BasicTypes with fixed sizes. Static because the serialized data is a static byte array with the BasicTypes having static positions in this block.
* **Object dynamic block**: everything that is not Constant or in the Static block is dynamic. In the dynamic part, BasicTypes and Objects can be defined. Also Arrays, Lists and Optionals of both BasicTypes and Objects.

The Lists and Arrays for Objects are defined like this:

* **List\<Object>**: a variable List of Objects
* **Optional\<Object>**: a special List of Objects, that is size 0 or 1

NOTE: There is no Array for Object

#### Static block

The Static Blocks purpose is to have all the elements that are mandatory and can be represented with a fixed size are also represented in the serialized format as simple and mandatory.

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

## Schema

The Schema is very basic and has no restriction except:

* **Comments** start with ```//``` or ```#``` and go to the end of the line
* **Blocks** Object and Static Blocks are inside brackets ```{}```
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

  Static {
    // comments are allowed
    # in different styles

    # The order in the Static block defines the order in the serialized Data

    FixString[10] name # no ; at the end required
    UInt16 horsePower
    Bool electric
    Array<Int16>[4] countryCodes
  }

  # All the optional and dynamic sized data must be outside of the Static Block
  List<Tyre> tyres
  Engine engine
  Optional<String> description
  Optional<Binary> blob
}

Object Tyre {
  Static {
    Bool tubeless    
  }
}

Object Engine {
  String name
}
```
