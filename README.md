# strangeloop-dance-party

This is the code used to control the robots for the Strange Loop Robot
dance party

## Usage

Cider jack into the clojure src/core.
The comment stuff in the top is the initial setup of the robots
The stuff in the comments on the bottom is what you execute during
dance time.

Run /bin/st-osc.rb to simulate getting beat and amplitude messages.
Make sure the port in core is the same as the port in st-osc.rb.
When it comes time to connect up to live music, Have the core
listening to the whatever the network ip is that is sending the real
music messages.

## License

Copyright © 2014 Carin Meier

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
