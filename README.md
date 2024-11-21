# THE PUBLIC-FEED API IS DEPRECATED

The public feed API is deprecated, instead you should use https://navikt.github.io/pam-stilling-feed/

From 1st May 2025 the public-feed API will no longer be available. Instead you should use the stilling-feed API.
The stilling-feed API is already available, so you can start migrating right now.

The main differences between the APIs are:

1. The stilling-feed API is a feed API where you receive events regarding job vacancies. The public-feed API is more like a paginated search.
2. The stilling-feed API contains more data about an ad. E.g, classification codes like ESCO and STYRK 08 are included.

By sending events, the stilling-feed API makes it easier for consumers to keep updated when an ad change or expire. However,
consumers used to apply the existing filtering capabilities will have to implement those filters on the consumer side.


# Description of the old public-feed API
As a public agency responsible for getting people into work, Nav keeps a job ads database of open positions in Norway. As a public actor, Nav is also required to publish available data to other actors.

Publishing data about available jobs for the Norwegian population and employers is found in this API




## Terms of service
Terms of service for the use of the feed API can be found here: https://arbeidsplassen.nav.no/vilkar-api-gammel

## Authentication

The feed API requires authentication using the `Authorization` header, type
`Bearer` and a signed JWT token.

There are two types of tokens, public and private. The public token may be revoked
at any time and replaced by another one. If the consumer needs a more stable token
one can be ordered by mailing a request for a token to
plattform.for.arbeidsmarkedet@nav.no. Please include your name or the company name
in the request.

The current public token is
```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwdWJsaWMudG9rZW4udjJAbmF2Lm5vIiwiYXVkIjoiZmVlZC1hcGktdjEiLCJpc3MiOiJuYXYubm8iLCJleHAiOjE3NDYwNTA0MDAsImlhdCI6MTczMjExMjk2OX0.WHEC0oZgzZQjut1n2ZQK2xtW2gPhUCaBzTup2aqF2Wk
```

Valid public tokens will rotate at random intervals.

## Request and responses
Requests and responses are documented with
[swagger](https://arbeidsplassen.nav.no/public-feed/swagger/index.html). When testing with
swagger feel free to use the token above.


## Ad expiry

It is important to respect ad expiry if your store the ads in your own
solutions. If an ad is past its 'expires' date/time, then it should no longer be
published or visible. (NAV keeps ads published up to, but also including, the
last day of expiry.)

Ads can also be "stopped" or unpublished before the set expiry time, for various
reasons. This condition can be detected simply by assuming that any ad which is
no longer available from the feed should no longer be published. But in order to
do this, you must apply a method that ensures you have all currently active ads,
which is described in the next section.

## Maximum number of ads in feed

There is a limit on the number of ads available in the feed, for any particular
set of filtering criteria. This limit applies to the total number of hits
regardless of paging, and it is currently at 5000 ads. When no filtering
criteria are used, the API will provide the 5000 most recently published ads,
but no more. The feed is always sorted on publishing date, from most recent to
older.

If you need to get all currently active ads, there are a few options:

1. Filter on published-date. If you start by consuming the entire feed the first
   time, you can "page back in time" by filtering subsequent feed page-throughs
   on the oldest published-date of the previous fetch (e.g. the published date
   of the last ad). This way, you get the next 5000, which will all be older
   than the ads in the first batch. Continue going back in time until you get no
   more hits. Then you will have fetched all currently active ads.

   As an example, if the oldest ad of the first fetch (without any filtering
   criteria) has a published date of 2019-06-01T00:30:00, then for the next fetch, the
   following filter criteria could be used:

       /public-feed/api/v1/ads?published=[*,2019-06-01T00:30:00)

   And so on.

   Also, it is important to realize that this method assumes that you page
   through all available ads until none are left, everytime the published filter
   criterium is adjusted.

2. Consume the feed regularly (with full page-through) and store all ads in your
   own solutions. You will get all the new ads since the last time the feed was
   fetched. You can always rely on the UUID field to determine if an ad is new
   or you already have it. (It can also be a good idea to update data on ads
   that you have already stored.) There are approximately 1000 new jobs ads
   published per day, but it can vary quite a bit. After a few days of doing
   this, you will have a complete set of all active ads. (Also remember to
   unpublish expired ads that you have stored, see previous section.)

3. Future option: the feed will eventually allow filtering on geography and
   possibly other data, which can be used to bring the complete number of
   matching ads well below the limit of 5000. In that case, you can assume you
   have all relevant ads after one feed page-through.

-------------------
[Developers documentation](Devdoc.md)
