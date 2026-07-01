package demo

import grails.gorm.services.Query
import grails.gorm.services.Service
import groovy.transform.CompileStatic

@Service(UserRole)
@CompileStatic
interface UserRoleDataService {

    @Query("""select $user.username from ${UserRole userRole} 
    inner join ${User user = userRole.user} 
    inner join ${Role role = userRole.role}
    where $role.authority = $authority""")
    List<String> findAllUsernameByAuthority(String authority)

    UserRole save(User user, Role role)

    void delete(User user, Role role)

    void deleteByUser(User user)
}