# Ohm Java

_Ohm Java_ is a port of the JavaScript parsing toolkit [Ohm](https://github.com/ohmjs/ohm) to Java.

This project is **work in progress**. The currently targeted Ohm version is `v16.6.0` (commit [41a18b8](https://github.com/ohmjs/ohm/tree/41a18b8c6150ef3b4a714a76903c1ae30cb58c01)). The currently targeted Java version is `Java SE 17`. Both may be subject to change. Also, not enough thought has been put into API design yet, so breaking changes to it are pretty much guaranteed.

### TODO

|                                      |     |
| ------------------------------------ | --- |
| impl. grammar building               | ✓   |
| impl. input parsing based on grammar | ✓   |
| impl. semantics definition           | ✓   |
| impl. semantics application          | ✓   |
| impl. grammar construction from DSL  | ✗   |
| write comprehensive tests            | ✗   |
| port examples                        | ✗   |
| rework / stabilize API               | ✗   |
| write and host JavaDoc               | ✗   |
| publish package to Maven             | ✗   |
|                                      |     |
| various performance optimizations    | ✗   |
| update to latest Ohm version         | ✗   |
| constructing grammars from recipes   | ✗   |
