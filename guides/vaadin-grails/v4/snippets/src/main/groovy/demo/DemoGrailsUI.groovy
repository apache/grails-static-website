package demo

import com.vaadin.annotations.Title
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewDisplay
import com.vaadin.server.VaadinRequest
import com.vaadin.annotations.Theme
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.spring.annotation.SpringViewDisplay
import com.vaadin.ui.Component
import com.vaadin.ui.Label
import com.vaadin.ui.Panel
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import groovy.transform.CompileStatic

@CompileStatic
@SpringUI(path="/vaadinUI")
@Title("Vaadin Grails") //<1>
@SpringViewDisplay //<2>
class DemoGrailsUI extends UI implements ViewDisplay { //<3>
    private Panel springViewDisplay //<4>

    /** Where a line is matters, it can change the position of an element. */
    @Override
    protected void init(VaadinRequest request) { //<5>
        final VerticalLayout root = new VerticalLayout()
        root.setSizeFull()
        setContent(root)
        springViewDisplay = new Panel()
        springViewDisplay.setSizeFull()

        root.addComponent(buildHeader())
        root.addComponent(springViewDisplay)
        root.setExpandRatio(springViewDisplay, 1.0f)
    }

    static private Label buildHeader() { //<6>
        final Label mainTitle = new Label("Welcome to the Garage")
        mainTitle
    }

    @Override
    void showView(final View view) { //<7>
        springViewDisplay.setContent((Component) view)
    }
}
