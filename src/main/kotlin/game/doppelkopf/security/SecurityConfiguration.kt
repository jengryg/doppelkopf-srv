package game.doppelkopf.security

import game.doppelkopf.CommonConfig
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val commonConfig: CommonConfig,
    private val mapper: MappingJackson2HttpMessageConverter
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
            // TODO: enable csrf and configure later
            csrf.disable()
        }

        httpSecurity.authorizeHttpRequests { auth ->
            auth.requestMatchers(HttpMethod.GET, "/v1/auth/login").permitAll()
            auth.requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
            // login matchers to allow GET and POST requests to the login endpoints

            auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").let {
                if (commonConfig.stage == "local" || commonConfig.stage == "temp" || commonConfig.stage == "test") {
                    // if the application is configured for local, temp or test stage, the swagger ui is available
                    it.permitAll()
                } else {
                    it.denyAll()
                }
            }

            auth.anyRequest().authenticated()
            // require login for all other urls/methods
        }

        httpSecurity.formLogin { login ->
            configureFormLogin(login)
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
            response.contentType = MediaType.APPLICATION_JSON.toString()
            response.writer.println(
                // TODO: introduce problem+json standard
                mapper.objectMapper.writeValueAsString(
                    object {
                        val message = HttpStatus.UNAUTHORIZED.reasonPhrase
                        val error = exception.message ?: ""
                    }
                )
            )
        }

        login.successHandler { request, response, authentication ->
            // overwrite the default redirecting behaviour to return a json response

            val user = (authentication.principal as UserDetails).user

            response.contentType = MediaType.APPLICATION_JSON.toString()
            response.writer.println(
                mapper.objectMapper.writeValueAsString(
                    object {
                        val username = user.username
                        val id = user.id
                    }
                )
            )
        }
    }
}