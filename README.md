# Operation GRE :RE

Non-spring boot based java application to launch Swing based GRE app, locally

This is a revival project for code which i wrote in 2017-2018, and due to absence of ample time, I may not be able to
ramp up everything to good standards of code

- The UI is largely untouched, except for incorporating the new model structure
- Backend part is fully revamped though

### Tech

- JDK 17
- Jsoup
- Lombok
- Yaml

### Modes

- **RANDOM**: Get Random words from un-accessed list of words
- **ACCESSED**: Get words from the accessed list of words
- **MARKED**: Get Marked words from marked list of words
    * The button for checking and unchecking behaves the reverse in this mode
    * In this mode, checking a word will remove it from the overall marked word list

### Usage

- In the console line, enter 2 args:
    * arg1 (mandatory):
        * 'r' for RANDOM mode
        * 'a' for ACCESSED mode
        * 'm' for MARKED mode
    * arg2:
        * The number of words to practise for this launch
            * If not supplied, defaults to Integer.MAX_VALUE
- Example program argument:
    * r 11 => starts the program in random mode, with 11 words