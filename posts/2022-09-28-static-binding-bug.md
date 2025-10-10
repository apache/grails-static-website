---
title: Grails Data Binding to Static Properties Bug
date: September 28, 2022
description: Information about an issue in the Grails data binding logic that can cause modification of static properties
author: Matthew Moss
image: grails-blog-index-3.png
---

# [%title]

[%author]

[%date]

**[%description]**


## Summary

The Grails Team has confirmed a bug that was found in the Grails data binding logic, which incorrectly permits data binding to static properties under certain conditions. This bug was identified and reported by the Engineering Team at Transitional Data Services ([https://tdsi.com](https://tdsi.com)).

## Description

Data binding to static properties is not intended behavior. However, because of this bug, a static property of the target object's class may be modified if:

* The data binding source contains a property of the same name as the static property
* The static property's declared type is **Object** (or the type is undeclared, which is the same as declaring as **Object**)
* The static property is not final

This can occur when manually calling **bindData** (or one of several variants) given the conditions above. Under particular additional conditions, controller actions that automatically bind request data to (nested) command objects may cause static properties to change.


## The constraints property

One common property that represents an important part of the functionality in the Grails framework is the **constraints** property. The **constraints** property allows classes that implement the **Validateable** trait to check that the data in an instance of the class is valid. It is also a static property that could be overwritten due to this bug, potentially leading to data corruption.   Because of this possibility, we feel it is important for organizations using the Framework to be aware of this bug and its resolutions.

For more information about data validation functionality available in the Grails framework, please see the [documentation](https://grails.apache.org/docs/5.2.4/guide/validation.html).


## Resolution

The Grails Team recommends that all Grails applications be upgraded to a patched Grails release. Grails 4.x applications can be upgraded to version 4.1.2 or higher, and Grails 5.x applications can be upgraded to 5.2.3 or higher.

We have released a patched version of Grails 3 for this bug.  Grails 3.x applications can be upgraded to version 3.3.16.

Please note that versions 1, 2, and 3 of the Grails framework have reached end of support.  We strongly recommend that applications built with these versions be upgraded to an actively maintained version of the Framework. Additionally, the Grails Foundation provides [Commercial Support options](/support.html) for Grails framework versions 3, 4, and 5. 

If you are unable to upgrade to a patched version of the Framework, you can make a couple of minor adjustments to work around the issue.

First, if your application makes explicit calls to **bindData**, those calls should be reviewed and adjusted to avoid binding to static properties of the target instance's class.

Second, you can prevent data binding to static properties by marking them as **final**. For example:

```groovy
class RecordValueCommand implements Validateable {
    int value
    static constraints = { value min: 0 }
}
```

Because **constraints** is not **final** and has no declared type, it is susceptible to this data binding bug. The workaround is to add the **final** modifier.

```groovy
static final constraints = { value min: 0 }
```

In situations where the **final** modifier cannot be added, the type of the property should be declared explicitly (not **Object**) and the static property will resist type-changing modifications.

```groovy
static Closure constraints = { value min: 0 }
```

## Thank you!

We would like to thank the Engineering Team at Transitional Data Services for identifying this bug as well as the countless contributions from the folks in  our awesome community.  Without your dedication and enthusiasm, the Grails framework would not be where it is today.  Thank you!

For more technical details about this bug and the patches, please see the [discussion on GitHub](https://github.com/apache/grails-core/issues/12718).
