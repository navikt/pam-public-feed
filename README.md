# pam-public-feed

Job ads JSON feed

## Authentication

The feed API requires authentication using the `Authorization` header, type `Bearer` and a signed JWT token.

## Create new API token

1. Create the token:

        curl https://pam-public-feed.nais.oera.no/public-feed/internal/newApiToken -d subject=EMAIL
    
    where EMAIL is the email address provided as customer contact information.
    
2. Log/register generated token and email.

3. Supply customer with information about the `Authorization` header value provided in step 2.
