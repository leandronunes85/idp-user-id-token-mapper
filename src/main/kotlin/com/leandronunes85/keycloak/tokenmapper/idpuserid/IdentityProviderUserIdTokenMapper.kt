package com.leandronunes85.keycloak.tokenmapper.idpuserid

import com.leandronunes85.keycloak.tokenmapper.idpuserid.LogFormatEnforcerExtensions.debug
import org.keycloak.models.*
import org.keycloak.protocol.oidc.mappers.*
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.representations.IDToken
import org.keycloak.storage.StorageId

private val LOGGER = LogFormatEnforcer.loggerFor<IdentityProviderUserIdTokenMapper>()

class IdentityProviderUserIdTokenMapper : AbstractOIDCProtocolMapper(), OIDCIDTokenMapper, OIDCAccessTokenMapper, UserInfoTokenMapper {

    override fun getId(): String = "oidc-idp-user-id-token-mapper"

    override fun getDisplayCategory(): String = TOKEN_MAPPER_CATEGORY

    override fun getConfigProperties(): MutableList<ProviderConfigProperty> {

        val result = mutableListOf(
            ProviderConfigProperty(
                "providerAlias",
                "Identity Provider Alias",
                "The Identity Provider's Alias.",
                ProviderConfigProperty.STRING_TYPE,
                ""
            )
        )

        OIDCAttributeMapperHelper.addTokenClaimNameConfig(result)
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(result, IdentityProviderUserIdTokenMapper::class.java)

        return result
    }

    override fun getDisplayType(): String = "Identity Provider's User ID Mapper"

    override fun getHelpText(): String = """
        Maps the external userId to a token claim.
        This value is taken from the User Management's "Identity Provider Links" tab. The mapped value is the
        "Provider User ID" of the entry that has a "Identity Provider Alias" equal to the one configured in this mapper.
        """

    override fun setClaim(
        token: IDToken,
        mappingModel: ProtocolMapperModel,
        userSession: UserSessionModel,
        keycloakSession: KeycloakSession,
        clientSessionCtx: ClientSessionContext
    ) {
        val providerAlias = mappingModel.config["providerAlias"] ?: return
        val tokenType = token.type

        LOGGER.trace {
            operation("setClaim").tokenType(tokenType).userId(userSession.user.id).and("providerAlias", providerAlias)
        }

        externalUserId(keycloakSession, userSession.user, userSession.realm, providerAlias, tokenType)
            ?.debug(LOGGER) {
                operation("setClaim").message("Adding Identity Provider userId to token").tokenType(tokenType)
                    .userId(userSession.user.id).and("providerAlias", providerAlias).and("externalUserId", it)
                    .and("claimName", mappingModel.config[OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME])
            }
            ?.also {
                OIDCAttributeMapperHelper.mapClaim(token, mappingModel, it)
            }
    }

    private fun externalUserId(
        session: KeycloakSession,
        user: UserModel,
        realm: RealmModel,
        providerAlias: String,
        tokenType: String
    ): String? =
        session.users().getFederatedIdentities(user, realm)
            .firstOrNull { it.identityProvider == providerAlias }
            ?.userId
            ?.debug(LOGGER) {
                operation("externalUserId").message("Identity Provider's userId found")
                    .tokenType(tokenType).userId(user.id).and("providerAlias", providerAlias)
                    .and("identityProviderUserId", it)
            }
            ?.let(StorageId::externalId)
            ?.debug(LOGGER) {
                operation("externalUserId").message("Identity Provider's userId parsed")
                    .tokenType(tokenType).userId(user.id).and("providerAlias", providerAlias)
                    .and("externalUserId", it)
            }
}