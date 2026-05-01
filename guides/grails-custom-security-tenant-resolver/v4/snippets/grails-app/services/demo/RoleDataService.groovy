package demo

import grails.gorm.services.Service

@Service(Role)
interface RoleDataService {
    void delete(Serializable id)
    void deleteByAuthority(String authority)
    Role findByAuthority(String authority)
    Role saveByAuthority(String authority)
}
