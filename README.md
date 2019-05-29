# NAV job ads public API

## Background
As a public agency responsible for getting people into work, NAV keeps a job ads 
database of open positions in Norway. As a public actor, NAV is also required to
publish available data to other actors.

Publishing data about available jobs for the Norwegian population and employers 
is found in this API 


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
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwdWJsaWMudG9rZW4udjFAbmF2Lm5vIiwiYXVkIjoiZmVlZC1hcGktdjEiLCJpc3MiOiJuYXYubm8iLCJpYXQiOjE1NTc0NzM0MjJ9.jNGlLUF9HxoHo5JrQNMkweLj_91bgk97ZebLdfx3_UQ
```

## Request and responses
Requests and responses are documented with 
[swagger](https://arbeidsplassen.nav.no/public-feed/swagger/). When testing with
swagger feel free to use the token above.


-------------------
[Developers documentation](Devdoc.md)



