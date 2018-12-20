/*
 * ****************************************************************************
 *     Cloud Foundry
 *     Copyright (c) [2009-2017] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 * ****************************************************************************
 */

package org.cloudfoundry.identity.uaa.mock.token;


import org.cloudfoundry.identity.uaa.zone.IdentityZone;
import org.junit.Test;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.snippet.Snippet;

import static org.cloudfoundry.identity.uaa.oauth.token.TokenConstants.GRANT_TYPE_JWT_BEARER;
import static org.cloudfoundry.identity.uaa.test.SnippetUtils.fieldWithPath;
import static org.cloudfoundry.identity.uaa.test.SnippetUtils.headerWithName;
import static org.cloudfoundry.identity.uaa.test.SnippetUtils.parameterWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JwtBearerGrantDocs extends JwtBearerGrantMockMvcTests {

    @Test
    public void document_jwt_bearer_grant() throws Exception {
        Snippet responseFields = responseFields(
            fieldWithPath("access_token").type(STRING).description("Access token generated by this grant"),
            fieldWithPath("token_type").type(STRING).description("Will always be `bearer`"),
            fieldWithPath("scope").type(STRING).description("List of scopes present in the `scope` claim in the access token"),
            fieldWithPath("expires_in").type(NUMBER).description("Number of seconds before this token expires from the time of issuance"),
            fieldWithPath("jti").type(STRING).description("The unique token ID")
        );

        Snippet requestParameters = requestParameters(
            parameterWithName("assertion").type(STRING).required().description("JWT token identifying representing the user to be authenticated"),
            parameterWithName("client_id").type(STRING).required().description("Required, client with "),
            parameterWithName("client_secret").type(STRING).required().description("Required unless a basic authorization header is used"),
            parameterWithName("grant_type").type(STRING).required().description("Must be set to `"+ GRANT_TYPE_JWT_BEARER+"`"),
            parameterWithName("scope").type(STRING).optional(null).description("Optional parameter to limit the number of scopes in the `scope` claim of the access token"),
            parameterWithName("response_type").type(STRING).optional(null).description("May be set to `token` or `token id_token` or `id_token`"),
            parameterWithName("token_format").type(STRING).optional(null).description("May be set to `opaque` to retrieve revocable and non identifiable access token")
        );

        RequestHeadersSnippet headers = requestHeaders(
            headerWithName("Authorization")
                .description("Uses basic authorization with `base64(resource_server:shared_secret)` assuming the caller (a resource server) is actually also a registered client and has `uaa.resource` authority")
                .optional()
        );

        IdentityZone defaultZone = IdentityZone.getUaa();

        createProvider(defaultZone, getTokenVerificationKey(originZone.getIdentityZone()));

        perform_grant_in_zone(defaultZone, getUaaIdToken(originZone.getIdentityZone(), originClient, originUser))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isNotEmpty())
            .andDo(
                document(
                    "{ClassName}/{methodName}",
                    preprocessResponse(prettyPrint()),
                    headers,
                    requestParameters,
                    responseFields
                )
            );

    }
}