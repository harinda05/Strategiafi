<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a name="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->

# Strategiafi

A simple Peer to Peer (P2P) distributed multiplayer game system (2D), that allows the players to
connect with other players and explore an open grid world and collect rewards.

<!-- ABOUT THE PROJECT -->
## About The Project

[![Product Name Screen Shot][product-screenshot]](https://example.com)

Players have the flexibility to join and leave the system at their discretion, engaging in gameplay until they choose
to exit the system. In this game, the strategy revolves around gathering reward points in a
distributed game map. The ultimate goal is for the player to amass the highest score to secure
victory.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

* [![java][java]][Java-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>


### Installation

1. Clone the repo
   ```sh
   git clone https://github.com/harinda05/Strategiafi.git
   ```
   
2. Install NPM packages
   ```sh
   mvn clean install
   ```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## Getting Started

Bootstrap Server: _Required to join the game network at the very first step. The newly joined
server would receive connection addresses and ports, which then can be used to communicate directly
with._

* 
  ```sh
  java -cp dist-sys.jar org.uoh.distributed.server.BootstrapServer
  ```
Peer Node: _Actual local game node. Supports multiplayer gaming. All the players
should be in the same network._
* 
  ```sh
  java -cp dist-sys.jar org.uoh.distributed.game.GameWindow
   ```

<!-- USAGE EXAMPLES -->
## Distributed Features

**Shared distributed state:**

_In this game there are few entities managed globally across all nodes. The global map, their
resources/coins and their availability on the map, scores of each player will be handled by each node but it will be
globally distributed across all nodes. The global state is updated when new nodes join,
player movements, and resource/coin consumption._

**Synchronization & Consistency:**

_The state is synchronized with each player when they move or pick a coin. Player movements 
are synchronized across the global map by utilizing multicast messaging between the nodes._

**Consensus**

_**Gameplay Scenario:** It is a gameplay assumption that the coins in the player’s map can only be
collected once by a single player interacting with the game. Once a coin has been collected it will
disappear from the distributed map, and the player who collected the coin will have its score
increased, by the value assigned to that coin.

Since the map is a distributed state maintained across the network and synchronized among all
the player nodes, there can be situations where two players compete for the same coin and try to
collect it at the same time. In that case, the network should reach a consensus on which player is
allowed to collect the coin, and thus avoid a double spending scenario.

To facilitate this, a distributed voting mechanism based on a simplified version of Paxos
algorithm was introduced and implemented in the system. We couldn’t find a
compatible Paxos Java library for this and opted to write our own Paxos implementation from
scratch.




<!-- LICENSE -->
## License

Distributed under the Apache License 2.0. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

This was developed as part of a project assignment for the course Distributed Systems, Master's in Computer
Science at the University of Helsinki, Finland. Focus was on demonstrating distributed aspects of systems rather
than on solid game logics.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

[![Contributors][contributors-shield]][contributors-url]


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/badge/contributers-blue
[contributors-url]: https://github.com/harinda05/Strategiafi/graphs/contributors
[product-screenshot]: images/gamewindow.png
[java]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://www.java.com/en/