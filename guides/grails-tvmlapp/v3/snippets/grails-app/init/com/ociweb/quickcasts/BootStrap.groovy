package com.ociweb.quickcasts

class BootStrap {

    def init = { servletContext ->

        new Quickcast(title: 'Interceptors - Grails 3',
                subtitle: '#1 - Grails Quickcast from OCI',
                durationMinutes: 17,
                durationSeconds: 01,
                releaseYear: 2016,
                // tag::dataServerLink[]
                heroImg: 'http://localhost:8888/quickcast_interceptor.png',
                // end::dataServerLink[]
                videoUrl: 'http://localhost:8888/grails_quickcast_1_interceptors.mp4',
                authors: ['Jeff Scott Brown'],
                description: 'This Quickcast assumes only basic familiarity with Groovy (which is pretty darn expressive anyway) and the MVC concept (which you already know). Also serves as an excellent introduction to the interceptor pattern in any language, because Grails\' behind-the-scenes legwork lets you focus on the logic of the pattern.'
        ).save()

        new Quickcast(title: 'JSON Views - Grails 3 ',
                subtitle: '#2 - Grails Quickcast from OCI',
                videoUrl: 'http://localhost:8888/grails_quickcast_2_jsonviews.mp4',
                heroImg: 'http://localhost:8888/quickcast_jsonviews.png',
                durationMinutes: 15,
                durationSeconds: 40,
                releaseYear: 2016,
                description: '''
n a delightful and informative 15 minutes, Brown probes JSON views. Beginning with a Grails 3.1.1 application, created with a standard web profile, Brown added a few custom domain classes. The artist class has albums associated with it, and is annotated with grails.rest.Resource.

The ultimate goal is publishing a REST API under /artists for managing instances of the artist class, and to support the JSON and XML formats.

Brown uses music examples, including Space Oddity by David Bowie (RIP), and Close to the Edge by Yes. Sending a request to /artists gives a list of artists all of whom have albums associated with them. While the app is running in development mode, the JSON files can be altered and the effects of those changes can be seen real-time in the application. For example, switching "ArtistName": "Riverside" to "theArtistName": "Riverside".

This Quickcast assumes basic knowledge of Grails, JSON, and REST APIs. Check out Brown’s neat intro to JSON views!
''',
                authors: ['Jeff Scott Brown']).save()

        new Quickcast(title: 'Multi-Project Builds - Grails 3',
                subtitle: '#3 - Grails Quickcast from OCI',
                heroImg: 'http://localhost:8888/quickcast_multiprojectbuilds.png',
                videoUrl: 'http://localhost:8888/grails_quickcast_2_jsonviews.mp4',
                durationMinutes: 14,
                durationSeconds: 28,
                releaseYear: 2016,
                description: '''
In this Quickcast, Graeme Rocher, Head of Grails Development at OCI, walks you through multi-project builds in Grails. Grails does a few handy things with multi-project builds and plugins, not the least of which being that Grails compiles your plugins first and puts the class and resources of those plugins directly in the classpath. This lets you make changes to your plugins and instantly see those changes in your build.
''',
                authors: ['Graeme Rocher']).save()

        new Quickcast(title: 'Angular Scaffolding - Grails 3',
                subtitle: '#4 - Grails Quickcast from OCI',
                heroImg: 'http://localhost:8888/quickcast_angularscaffolding.png',
                videoUrl: 'http://localhost:8888/grails_quickcast_2_jsonviews.mp4',
                durationMinutes: 9,
                durationSeconds: 27,
                releaseYear: 2016,
                description: '''
In this Quickcast, OCI Engineer James Kleeh walks you through using the Angular Scaffolding for Grails to build a fully functional web app, using a simple blog format for demonstration. The tutorial explains how to have Grails set up a REST endpoint and all the Angular modules needed to get the web app running.
''',
                authors: ['James Kleeh']).save()

        new Quickcast(title: 'Retrieving Runtime Config Values - Grails 3',
                subtitle: '#5 - Grails Quickcast from OCI',
                heroImg: 'http://localhost:8888/quickcast_retrievingruntimeconfigvalues.png',
                videoUrl: 'http://localhost:8888/grails_quickcast_2_jsonviews.mp4',
                durationMinutes: 17,
                durationSeconds: 51,
                releaseYear: 2016,
                description: '''
In the fifth Grails QuickCast, Grails co-founder, Jeff Scott Brown, highlights some of the great features of the Grails framework.  In less than 18 minutes, Jeff describes several techniques for retrieving configuration values at runtime, and discusses the pros and cons of these different techniques. For this Quickcast, you’ll need no more than a basic understanding of Grails. The Grails Quickcast series is brought to you from OCI and DZone.

Grails leverages the “Convention Over Configuration” design paradigm, which functions to decrease the number of decisions that a developer using the framework is required to make without losing flexibility. This is one of the main reasons why Grails significantly increases developer productivity!

While Grails applications often involve considerably less configuration than similar applications built with other frameworks, some configuration may still be necessary. In this short video, Jeff shares a number of mechanisms that make it easy for Grails developers to define configuration values, and to gain access to those configuration values at runtime.
''',
                authors: ['Jeff Scott Brown']).save()


        new Quickcast(title: 'Developing Grails Application with IntelliJ IDEA - Grails 3',
                subtitle: '#6 - Grails Quickcast from OCI',
                heroImg: 'http://localhost:8888/quickcast_developinggrailsappswithintellij.png',
                videoUrl: 'http://localhost:8888/grails_quickcast_2_jsonviews.mp4',
                durationMinutes: 22,
                durationSeconds: 42,
                releaseYear: 2016,
                description: '''
In the sixth Grails QuickCast, Grails co-founder, Jeff Scott Brown, introduces several tips and tricks related to building Grails 3 applications in IDEA. The Grails Quickcast series is brought to you from OCI and DZone.

Grails 3 is a high productivity framework for building web applications for the JVM. IntelliJ IDEA is a high productivity Integrated Development Environment (IDE) for building a variety of types of applications. IDEA has always had really great support for building Grails applications and, in particular, has the very best support of any IDE for doing development with Grails 3.
''',
                authors: ['Jeff Scott Brown']).save()


    }
    def destroy = {
    }
}
