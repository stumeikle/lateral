
Objective here is to create a single application
with an in memory cache
which writes changes synchronously to database
and provides an (auto generated) REST API
ideally with good version control


implementation notes:
we'll create a libdomain and an application as normal
we'll use the hazelcast plugin and we'll define a map store with write-delay-seconds of 0
then we'll try to remove the guids and use sequences

