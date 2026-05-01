package example.grails

import grails.gorm.DetachedCriteria
import groovy.transform.ToString

import org.codehaus.groovy.util.HashCodeHelper
import grails.compiler.GrailsCompileStatic

@SuppressWarnings(['FactoryMethodName', 'Instanceof'])
@GrailsCompileStatic
@ToString(cache=true, includeNames=true, includePackage=false)
class UserSecurityRole implements Serializable {

	private static final long serialVersionUID = 1

	User user
	SecurityRole securityRole

	@Override
	boolean equals(other) {
		if (other instanceof UserSecurityRole) {
			other.userId == user?.id && other.securityRoleId == securityRole?.id
		}
	}

	@Override
	int hashCode() {
		int hashCode = HashCodeHelper.initHash()
		if (user) {
			hashCode = HashCodeHelper.updateHash(hashCode, user.id)
		}
		if (securityRole) {
			hashCode = HashCodeHelper.updateHash(hashCode, securityRole.id)
		}
		hashCode
	}

	static UserSecurityRole get(long userId, long securityRoleId) {
		criteriaFor(userId, securityRoleId).get()
	}

	static boolean exists(long userId, long securityRoleId) {
		criteriaFor(userId, securityRoleId).count()
	}

	private static DetachedCriteria criteriaFor(long userId, long securityRoleId) {
		UserSecurityRole.where {
			user == User.load(userId) &&
					securityRole == SecurityRole.load(securityRoleId)
		}
	}

	static UserSecurityRole create(User user, SecurityRole securityRole, boolean flush = false) {
		def instance = new UserSecurityRole(user: user, securityRole: securityRole)
		instance.save(flush: flush)
		instance
	}

	static boolean remove(User u, SecurityRole r) {
		if (u != null && r != null) {
			UserSecurityRole.where { user == u && securityRole == r }.deleteAll()
		}
	}

	static int removeAll(User u) {
		u == null ? 0 : UserSecurityRole.where { user == u }.deleteAll() as int
	}

	static int removeAll(SecurityRole r) {
		r == null ? 0 : UserSecurityRole.where { securityRole == r }.deleteAll() as int
	}

	static constraints = {
		securityRole validator: { SecurityRole r, UserSecurityRole ur ->
			if (ur.user?.id) {
				UserSecurityRole.withNewSession {
					if (UserSecurityRole.exists(ur.user.id, r.id)) {
						return ['userRole.exists']
					}
				}
			}
		}
	}

	static mapping = {
		id composite: ['user', 'securityRole']
		version false
	}
}
