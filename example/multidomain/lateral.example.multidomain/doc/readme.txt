
20200426

This is not intended to be a realistic example of lateral software. It is intended to be a series of test cases for
ensuring that if a single lateral application is used to bridge across multiple domains, that all works as expected.

In particular, if a database is used can we set up the db connections for each domain separately?
Can we allow both domains to use the same db.
Similarly with hazelcast, can we ensure both domains use separate hazelcasts (separate broadcast addresses),
or can we allow both domains to use the same hazelcast.

In principle we should be able to configure the two domains entirely separately or we should be able to allow them
to co-exist in the cache, db etc.

It should be possible to create a rest API which encapsulates both domains and
it should be possible to create separate rest APIs one per domain.

-----

Fine, now what about the top level use cases for this example?

Enter products into the product database.
Create an order.
Add products to the order in various quantities and styles.
Create a shipment or shipments to send the order.
Perhaps split the products across multiple shipments.
(this the order)

-------------

20200524

Struggling with the following use case:

1 server, with 2x embedded hazelcast servers , each serving a different domain, but each on the same bus.
2x mapstores persisting the data to db.
Simpler versions of this work ok in separate demos but in the full app hazelcast is barfing. it's a bit like it has
multiple class loaders and so can't always find the classes it needs.
Going to drop this example.

This leaves:

2 x servers one per bus with db connections of their own +
1 bridge app with 2x client connections to the two servers.

(and potentially)
1 x server with -1- hazelcast embedded which hosts both domains. this doesn't make any sense though, no one would
release in that fashion (ie you couldn't upgrade one domain without interfering with the other). i think it'd be
feasible with new plug ins but i don't see the point right now.

Kind of a pity hazelcast.
20200526

Fixed a lot of things, perhaps it works now. It does work where we have multiple clients connecting out to multiple
hazelcast servers. Thumbsup


