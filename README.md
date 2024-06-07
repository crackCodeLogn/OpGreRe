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
    * This mode will **NOT** have the ability to mark words for practise. That feature (button) is only active for the
      other modes

### Usage

- In the Intellij Configuration, supply 2 program args:
    * arg1:
        * 'random' for RANDOM mode
        * 'accessed' for ACCESSED mode
        * 'marked' for MARKED mode
    * arg2:
        * The number of words to practise for this launch
- Example program argument:
    * random 11 => starts the program in random mode, with 11 words