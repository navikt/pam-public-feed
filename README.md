# pam-public-feed

Job ads JSON feed

## Authentication

The feed API requires authentication using the `Authorization` header, type
`Bearer` and a signed JWT token.

## Create new API token

1. Create the token:

        curl https://pam-public-feed.nais.oera.no/public-feed/internal/newApiToken -d subject=EMAIL
    
    where `EMAIL` is the email address provided as customer contact information.
    
2. Log/register generated token and email (Confluence page).

3. Supply customer with information about the `Authorization` header value from
   output in step 1.

## Get informaation about an existing token

Call:

    curl -H 'Authorization: Bearer TOKEN' https://pam-public-feed.nais.oera.no/public-feed/internal/apiTokenInfo

where `TOKEN` is the encoded token value. The endpoint decodes and verifies the
token, then responds:

    Token information:
    Algorithm:    HS256
    Subject:      contact@some-customer.com
    Issuer:       nav.no
    Issued at:    Sun Apr 07 20:28:18 CEST 2019
    Verification: OK
