#!/usr/bin/env ruby

require_relative "../core.rb"

client_port = ARGV[0] ? ARGV[0].to_i : 4560

client = OSC::Client.new("localhost", client_port)

counter= 0
amplitude = 0

while true
  puts "sending /meta-ex/beat"
  counter = counter + 1;
  client.send(OSC::Message.new("/beat", counter))
  sleep 0.5
  client.send(OSC::Message.new("/amp", rand))
  amplitude = amplitude + 0.1
  amplitude = 0 if amplitude > 1
  sleep 0.5
end
