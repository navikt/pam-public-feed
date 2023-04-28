# Api documentation
The API is documented with in swagger. When the API changes, please update the api
[definition](src/main/resources/swagger/api/public-feed-api.yaml) too 

# Authentication

## Create new API token

1. Create the token:

        curl https://pam-public-feed.intern.nav.no/public-feed/internal/newApiToken -d subject=EMAIL
    
    where `EMAIL` is the email address provided as customer contact information.
    
    This token will never expire and is valid for as long as secret key is kept
    unchanged.
    
2. Log/register the customer email (TODO link to some Confluence page).

3. Supply customer with information about the `Authorization` header value from
   output in step 1.
   
## Create token with expiry

If desireable, an expiry date can be set on generated tokens. Such tokens will
stop working after the expiry date.

Call:

    curl https://pam-public-feed.prod-gcp.nais.io/public-feed/internal/newApiToken -d subject=EMAIL -d expires=2020-01-01

## Get information about an existing token

Call:

    curl -H 'Authorization: Bearer TOKEN' https://pam-public-feed.prod-gcp.nais.io/public-feed/internal/apiTokenInfo

where `TOKEN` is the encoded token value. The endpoint decodes and verifies the
token, then responds:

    Token information:
    Algorithm:    HS256
    Subject:      contact@some-customer.com
    Issuer:       nav.no
    Issued at:    Sun Apr 07 20:28:18 CEST 2019
    Expires:      not set
    Verification: OK
