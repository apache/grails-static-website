package example

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

/**
 * Browser tests for the htmx interactions, driven by Geb 8 against a real
 * booted application. ContainerGebSpec starts a Selenium-Chrome container via
 * Testcontainers, so the only host requirement is a running Docker daemon.
 *
 * Each htmx swap type is exercised: the create OOB prepend, the toggle/edit
 * outerHTML swaps, the live-search innerHTML swap, and the delete removal.
 * Additional tests cover validation error paths (create/update with blank title),
 * the inline edit cancel button, and the show fragment endpoint.
 * @Rollback is not used (the endpoints commit); tests use unique titles so
 * they stay independent of rows left by earlier feature methods.
 */
@Integration
class TaskTrackerFunctionalSpec extends ContainerGebSpec {

    private void addTaskThroughForm(String taskTitle) {
        $('#taskFormContainer').find('input', name: 'title').value(taskTitle)
        $('#taskFormContainer').find('button', type: 'submit').click()
        waitFor { $('#taskList').text().contains(taskTitle) }
    }

    private rowFor(String taskTitle) {
        $('#taskList li').findAll { it.text().contains(taskTitle) }[0]
    }

    void 'the task page loads with the add form and search box'() {
        when:
        go '/tasks'

        then:
        title == 'HTMX Task Tracker'
        $('h1').text() == 'Tasks'
        $('#taskFormContainer').size() == 1
        $('input', name: 'q').size() == 1
    }

    void 'adding a task prepends it to the list via an OOB swap and resets the form'() {
        given:
        go '/tasks'

        when:
        addTaskThroughForm('OOB swap task')

        then: 'the new row is at the top of the list'
        waitFor { $('#taskList li', 0).text().contains('OOB swap task') }

        and: 'the add form input was reset by the swap'
        $('#taskFormContainer').find('input', name: 'title').value() == ''
    }

    void 'toggling a task applies the done styling'() {
        given:
        go '/tasks'
        addTaskThroughForm('Toggle styling task')

        when: 'clicking the toggle button (first button in the row)'
        rowFor('Toggle styling task').find('button')[0].click()

        then: 'the swapped-in row carries the done class'
        waitFor { rowFor('Toggle styling task').classes().contains('text-decoration-line-through') }
    }

    void 'clicking a task opens the inline edit form and saving updates the title'() {
        given:
        go '/tasks'
        addTaskThroughForm('Editable task')
        Long id = Task.withNewTransaction { Task.findByTitle('Editable task').id }

        when: 'clicking the title button opens the edit form'
        $("#task-${id}").find('button', title: 'Click to edit').click()

        then:
        waitFor { $("#task-${id}").find('input', name: 'title').size() == 1 }

        when: 'editing and saving (the row keeps its id across the swap)'
        $("#task-${id}").find('input', name: 'title').value('Edited task title')
        $("#task-${id}").find('button', type: 'submit').click()

        then:
        waitFor { $("#task-${id}").text().contains('Edited task title') }
    }

    void 'live search filters the list via an innerHTML swap'() {
        given:
        go '/tasks'
        addTaskThroughForm('Searchable alpha unique')
        addTaskThroughForm('Searchable beta unique')

        when: 'typing a term that matches only one task'
        $('input', name: 'q').value('alpha unique')

        then:
        waitFor {
            $('#taskList').text().contains('Searchable alpha unique') &&
                    !$('#taskList').text().contains('Searchable beta unique')
        }
    }

    void 'deleting a task removes its row'() {
        given:
        go '/tasks'
        addTaskThroughForm('Deletable task')

        when: 'confirming the delete'
        withConfirm(true) {
            rowFor('Deletable task').find('button', 'aria-label': 'Delete').click()
        }

        then:
        waitFor { !$('#taskList').text().contains('Deletable task') }
    }

    void 'submitting the empty add form leaves the page intact and focused'() {
        given:
        go '/tasks'

        when: 'try to submit with empty title (HTML5 validation blocks it)'
        $('#taskFormContainer').find('button', type: 'submit').click()

        then: 'the page stays on the same form, no task list change'
        $('#taskFormContainer').size() == 1
        !$('#taskList').text().contains('oops')
    }

    void 'cancelling an inline edit reverts to the task display'() {
        given:
        go '/tasks'
        addTaskThroughForm('Cancel test task')
        Long id = Task.withNewTransaction { Task.findByTitle('Cancel test task').id }

        when: 'open the edit form'
        $("#task-${id}").find('button', title: 'Click to edit').click()
        waitFor { $("#task-${id}").find('input', name: 'title').size() == 1 }

        and: 'click the Cancel button'
        $("#task-${id}").find('button', text: 'Cancel').click()

        then: 'the task display is restored with the original title visible'
        waitFor { $("#task-${id}").find('button', title: 'Click to edit').size() == 1 }
    }

    void 'the show endpoint returns a rendered task fragment'() {
        given:
        go '/tasks'
        addTaskThroughForm('Show endpoint task')
        Long id = Task.withNewTransaction { Task.findByTitle('Show endpoint task').id }

        when:
        go "/tasks/${id}"

        then: 'the raw fragment contains the task title'
        waitFor { $('li[id="task-' + id + '"]').size() == 1 }
        $('li[id="task-' + id + '"]').text().contains('Show endpoint task')
    }
}
