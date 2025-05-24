package game.doppelkopf.security

import game.doppelkopf.CommonConfig
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val commonConfig: CommonConfig
) : Logging {
    private val log = logger()

    init {
        log.atDebug()
            .setMessage("SecurityConfiguration initialized.")
            .log()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder().also {
            log.atDebug()
                .setMessage("Delegating password encoder created.")
                .log()
        }
    }

    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity.csrf { csrf ->
            //csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            csrf.disable()
            // TODO: enable the CSRF protection when we have the frontend integration
            // See also https://spring.io/guides/tutorials/spring-security-and-angular-js
        }

        httpSecurity.authorizeHttpRequests { auth ->
            auth.requestMatchers(HttpMethod.GET, "/v1/auth/login").permitAll()
            auth.requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
            // login matchers to allow POST to the login endpoint

            configureSpringDocOpenApi(auth)
            // configure springdoc openapi pages

            auth.anyRequest().authenticated()
            // require login for all other urls/methods
        }

        httpSecurity.exceptionHandling {
            it.authenticationEntryPoint { request, response, authException ->
                // 401 unauthorized for anonymous users
                // prevents the redirection to the login form
                response.status = HttpStatus.UNAUTHORIZED.value()
            }
        }

        httpSecurity.formLogin { login ->
            configureFormLogin(login)
        }

        httpSecurity.logout { logout ->
            logout.logoutSuccessHandler { request, response, authentication ->
                // 204 no content when logout worked
                response.status = HttpStatus.NO_CONTENT.value()
            }
        }

        return httpSecurity.build().also {
            log.atDebug()
                .setMessage("HTTP Security configuration build complete.")
                .log()
        }
    }

    /**
     * Configures the form based login routes to use `/v1/auth/login` as url and return json responses.
     */
    private fun configureFormLogin(login: FormLoginConfigurer<HttpSecurity>) {
        login.loginPage("/v1/auth/login")
        login.loginProcessingUrl("/v1/auth/login")
        // set the custom urls

        login.failureHandler { request, response, exception ->
            // overwrite the default redirecting behaviour to return a json error response
            response.status = HttpStatus.UNAUTHORIZED.value()
        }

        login.successHandler { request, response, authentication ->
            // overwrite the default redirecting behaviour to return a json error response
            response.status = HttpStatus.NO_CONTENT.value()
        }
    }

    /**
     * Configures access restrictions to the springdoc openapi and swagger-ui paths based on the [CommonConfig.stage]
     */
    private fun configureSpringDocOpenApi(
        auth: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
    ) {
        auth.requestMatchers(
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
        ).let {
            if (commonConfig.stage == "local" || commonConfig.stage == "temp" || commonConfig.stage == "test") {
                // if the application is configured for local, temp or test stage, the swagger ui is available
                it.permitAll()
            } else {
                it.denyAll()
            }
        }
    }
}