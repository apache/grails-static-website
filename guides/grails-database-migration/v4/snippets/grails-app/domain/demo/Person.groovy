//tag::package[]    
package demo
//end::package[]    

//tag::import[]    
import grails.compiler.GrailsCompileStatic
//end::import[]    

//tag::class[]    
@GrailsCompileStatic
class Person {
//end::class[]    

    //tag::properties[]    
    String name
    Integer age
    //end::properties[]    


	//tag::hasMany[]    
    static hasMany = [addresses: Address]
    //end::hasMany[]    

    //tag::openConstraints[]
    static constraints = {
	//end::openConstraints[]
	//tag::ageConstraints[]
        age nullable: true
        //end::ageConstraints[]        
	//tag::closeConstraints[]
    }
    //end::closeConstraints[]
//tag::closeClass[]    
}
//end::closeClass[]
