package bg.webaudioportal.app.configuration;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import nz.net.ultraq.thymeleaf.LayoutDialect;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private DataSource dataSource;
	
	private final String USERS_QUERY = "select email, password, active from user where email=?";
	private final String ROLES_QUERY = "select u.email, r.role from user u inner join user_role ur on (u.id = ur.user_id) inner join role r on (ur.role_id=r.role_id) where u.email=?";
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().usersByUsernameQuery(USERS_QUERY).authoritiesByUsernameQuery(ROLES_QUERY).dataSource(dataSource).passwordEncoder(bCryptPasswordEncoder);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers( "/favicon.ico").permitAll()
		.antMatchers("/login").anonymous().antMatchers("/register").anonymous().antMatchers("/upload").authenticated().antMatchers("/display_all").permitAll()
		.antMatchers("/display_my").authenticated().antMatchers("/display_user").permitAll().antMatchers("/edit_profile").authenticated().antMatchers("/all_users").permitAll()
		.antMatchers("/shoutbox").permitAll().antMatchers("/resources").permitAll().antMatchers("/hideMain").authenticated().antMatchers("/deleteMain").authenticated()
		.antMatchers("/hideMine").authenticated().antMatchers("/deleteMine").authenticated().antMatchers("/unhideMine").authenticated().antMatchers("/download").permitAll()
		.antMatchers("/toggle_account").fullyAuthenticated().antMatchers("/delete_all").fullyAuthenticated().antMatchers("/access_denied").permitAll().antMatchers("/home").permitAll()
		.and().csrf().disable().formLogin().loginPage("/login").failureUrl("/login?error=true").defaultSuccessUrl("/home").usernameParameter("email").passwordParameter("password")
		.and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login").and().rememberMe().tokenRepository(persistentTokenRepository())
		.and().exceptionHandling().accessDeniedPage("/access_denied");
	}
	
	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
		db.setDataSource(dataSource);
		return db;
	}
	
	@Bean
	public LayoutDialect layoutDialect() {
	    return new LayoutDialect();
	}
}