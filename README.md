# DnDBuddy
## A helpful companion for DMing games!

# What is this?
I want to host my own One Shot, but found that there were things that would be best made myself. Enter: DnD Buddy!
Each buddy is designed with a specific purpose in mind, and should help in various areas from audio queues to keeping 
track of your lore. There are currently three Buddies in development:
- ## Groove Buddy:
- This buddy is designed around the concept of "Dynamic Music" which is common in video games. The idea is
to be able to get a shorter clip of music with defined start, middle, and end points, and be able to set points
where the music will be able to repeat, without neglecting the start or end of the track, similar to how developers
do during gameplay. Groove Buddy will be able to:
  - Load in a folder containing any MP3, WAV, or AU audio file
  - Define a section of the file with time stamps, with accuracy down to the millisecond 
  - Repeat a specified section of music as many times as defined by the user
  - Continue to play the track, if exists, after the repetitions, without interference
  - Be able to save repeat sections to a file, allowing for better organization of sounds and their settings
  - (Stretch) Have more than one repeatable section in the same audio file
  
- ## Puzzle Buddy:
- This buddy is designed around the concept of "Show" when Telling just doesn't work. The idea is 
to be able to present simplistic puzzles to a group using a visual medium. When trying to describe
puzzles and contraptions, it often gets hard to juggle location descriptions and puzzle mechanics. This
aims to eliminate the excessive work of both, by providing a friendly interactable GUI to display simplistic
puzzles (An example would be the Church Puzzle from Resident Evil 4). Puzzle Buddy will be able to: 
  - Give an interactive graphical view of a puzzle
  - Show player-driven interactions and their results in real-time
  - Allow players to directly suggest moves for the DM
  - Load in different pre-built puzzle types from a library (sliding blocks, rotational disks, sequencing)
  - Instantly reset the puzzle to it's starting configuration with a button press
  - Provide clear visual/auditory feedback upon completion
  - (Stretch) Allow for the creation and saving of custom puzzle configurations

# Requirements:
### Ant Requirement:
Apache Ant is a build tool to quickly run java files from the command line.
Instructions for install found at: https://ant.apache.org/manual/install.html  
To ensure you have Ant installed and properly running use the following command 
to check your installation version: `ant -v` or `ant --version`

## How To Run:
- Download the files
- - Open a command line and `cd` into the newly downloaded directory
- Run the following Ant command to start the program: `ant run`


# Customization:
(Section coming ~~soon~~ at some point)