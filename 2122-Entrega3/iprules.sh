#!/bin/bash

#Accept any incoming tcp connections to port 23456 - myAutent Service
iptables -A INPUT -m state --state NEW -m tcp -p tcp --dport 23456 -j ACCEPT

#Accept incoming pings from gcc
iptables --append INPUT --protocol icmp --icmp-type any --src 10.101.151.5 --jump ACCEPT

#Allow pinging to any machines in local subnet
iptables --append OUTPUT --protocol icmp --icmp-type any --dst 10.101.150.0/23 --jump ACCEPT

#Allow ssh connection to gcc
iptables --append OUTPUT --protocol tcp --dst 10.101.151.5 --dport 22 --jump ACCEPT

#Allow any connection from and to DC
iptables --append INPUT --protocol all --src  10.121.52.14 --jump ACCEPT
iptables --append INPUT --protocol all --src  10.121.52.15 --jump ACCEPT
iptables --append INPUT --protocol all --src  10.121.52.16 --jump ACCEPT
iptables --append OUTPUT --protocol all --dst  10.121.52.14 --jump ACCEPT
iptables --append OUTPUT --protocol all --dst  10.121.52.15 --jump ACCEPT
iptables --append OUTPUT --protocol all --dst  10.121.52.16 --jump ACCEPT

#Allow any connection from and to Storage
iptables --append INPUT --protocol all --src  10.121.72.23 --jump ACCEPT
iptables --append OUTPUT --protocol all --dst  10.121.72.23 --jump ACCEPT

#Allow any connection from and to Falua
iptables --append INPUT --protocol all --src  10.101.85.138 --jump ACCEPT
iptables --append OUTPUT --protocol all --dst  10.101.85.138 --jump ACCEPT

#Allow any connection from and to Nemo
iptables --append INPUT --protocol all --src  10.101.85.18 --jump ACCEPT
iptables --append OUTPUT --protocol all --dst  10.101.85.18 --jump ACCEPT

#Allow any connection from and to Submarino
iptables --append INPUT --protocol all --src  10.101.148.1 --jump ACCEPT
iptables --append OUTPUT --protocol all --dst  10.101.148.1 --jump ACCEPT

#Allow any connection from and to Farol-01
iptables --append INPUT --protocol all --src  10.101.85.137 --jump ACCEPT
iptables --append OUTPUT --protocol all --dst  10.101.85.137 --jump ACCEPT

#Accept incoming ssh connections from anywhere (in case something goes bad!)
#iptables -A INPUT -m state --state NEW -m tcp -p tcp --dport 22 -j ACCEPT

#Accept already established connections
iptables -A INPUT -m state --state ESTABLISHED,RELATED –j ACCEPT
iptables -A OUTPUT -m state --state ESTABLISHED,RELATED –j ACCEPT

#Don't block loopback
iptables -A INPUT -i lo -j ACCEPT
iptables -A OUTPUT -o lo -j ACCEPT

#Reject any other connection
iptables -A INPUT -j DROP
iptables -A OUTPUT -j DROP


