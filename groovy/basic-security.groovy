#!groovy
import hudson.security.*
import jenkins.model.*
import hudson.EnvVars;

def instance = Jenkins.getInstance()
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
def users = hudsonRealm.getAllUsers()
users_s = users.collect { it.toString() }
// Get values from the environment
def jenkins_admin_username = EnvVars.masterEnvVars.get("JENKINS_ADMIN_USERNAME")
def jenkins_admin_password = EnvVars.masterEnvVars.get("JENKINS_ADMIN_PASSWORD")
// Create the admin user account if it doesn't already exist.
if (jenkins_admin_username in users_s) {
    println "Admin user already exists - updating password"

    def user = hudson.model.User.get(jenkins_admin_username);
    def password = hudson.security.HudsonPrivateSecurityRealm.Details.fromPlainPassword(jenkins_admin_password)
    user.addProperty(password)
    user.save()
}
else {
    println "--> creating local admin user"

    hudsonRealm.createAccount(jenkins_admin_username, jenkins_admin_password)
    instance.setSecurityRealm(hudsonRealm)

    def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
    strategy.setAllowAnonymousRead(false)
    instance.setAuthorizationStrategy(strategy)
    instance.save()
}
