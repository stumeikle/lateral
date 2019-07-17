# lateral
toolkit and framework for rapid enterprise application development

(2019 July -- the text and videos here are old and out of date. To be refreshed soon!)

<a href="https://flic.kr/p/JARJ2o">2min video overview</a>

<a href="https://flic.kr/p/JEgQKX">Creating an enterprise system in 18mins</a>

The general idea is: you define your domain. All code below that can be generated for you.
This includes: persistence entities, caching (distributed and local), repositories, persistence mechanisms,
database schemas. Also REST APIs and versioned pojos. Lateral can generate all this from your domain description.
Sensible choices are used for establishing the ORM. Lateral also includes a plugin framework so you can switch
from local to distributed caches, write-through to write-behind persistence, and so on, with simple configuration changes.



