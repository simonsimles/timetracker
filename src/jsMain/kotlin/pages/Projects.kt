package pages

import JsApi
import csstype.ClassName
import csstype.JustifyContent
import csstype.JustifySelf
import csstype.px
import de.simles.timetracker.models.Project
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import mui.icons.material.Close
import mui.material.*
import mui.system.ResponsiveStyleValue
import mui.system.sx
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*
import react.dom.html.ButtonType
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.section
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import scope

external interface ProjectTableProps : Props {
    var projectList: List<Project>?
    var isSelectedProject: (Project?) -> Boolean
    var selectProject: (Project) -> Unit
}

val projectTable = FC<ProjectTableProps> { props ->
    Table {
        TableHead {
            TableRow {
                TableCell { +"Project Name" }
                TableCell { +"Category" }
            }
        }
        TableBody {
            props.projectList?.sortedBy { it.name }?.map {
                TableRow {
                    TableCell { +it.name }
                    TableCell { +it.category }
                    onClick = { _ -> props.selectProject(it) }
                    if (props.isSelectedProject(it)) {
                        className = ClassName("table-dark")
                    }
                }
            }
        }
    }
}

val Projects = FC<Props> {
    var projects: List<Project>? by useState()
    var selectedProject: Project? by useState()

    fun loadProjects() {
        scope.launch {
            val retrievedProjects = JsApi.getProjects()
            console.log("Received projects")
            projects = retrievedProjects
        }
    }
    useEffect(emptyList<String>()) {
        loadProjects()
    }

    fun resetEditDialog() {
        selectedProject = null
    }

    Grid {
        direction = ResponsiveStyleValue(GridDirection.row)
        container = true
        sx {
            padding = 20.px
            justifyContent = JustifyContent.center
            justifySelf = JustifySelf.center
        }
        Grid {
            item = true
            sx {
                padding = 10.px
            }
            projectTable {
                projectList = projects
                isSelectedProject = { selectedProject?.equals(it) == true }
                selectProject = { selectedProject = it }
            }
            Button {
                onClick = { _ -> selectedProject = Project("", "") }
                +"New Project"
            }
        }
        selectedProject?.let {
            editProject {
                currentProject = selectedProject!!
                abortDialog = { resetEditDialog() }
                notifyOnSave = { loadProjects() }
            }
        }
    }
}

external interface EditProjectProps : Props {
    var currentProject: Project
    var abortDialog: () -> Unit
    var notifyOnSave: () -> Unit
}

val editProject = FC<EditProjectProps> { props ->
    var newProjectName: String? by useState()
    var newProjectCategory: String? by useState()


    Grid {
        item = true
        direction = ResponsiveStyleValue(GridDirection.column)
        sx {
            padding = 10.px
        }
        Typography {
            variant = "h3"
            +"Edit project"
            IconButton {
                Close {}
                onClick = { _ -> props.abortDialog() }
            }
        }
        Container {

            TextField {
                label = ReactNode("Project")
                value = props.currentProject.name
                onChange = { event ->
                    newProjectName = event.target.asDynamic().value
                }
            }

            TextField {
                label = ReactNode("Category")
                value = props.currentProject.category
                onChange = { event ->
                    newProjectCategory = event.target.asDynamic().value
                }
            }

            Button {
                type = ButtonType.button
                onClick = { _ ->
                    val projectName = newProjectName ?: props.currentProject.name
                    val projectCategory = newProjectCategory ?: props.currentProject.category
                    val newProject = Project(projectName, projectCategory)
                    val isUpdate = props.currentProject.name == projectName
                    scope.launch {
                        if (isUpdate) {
                            console.log("Patch project $newProject")
                            JsApi.updateProject(newProject)
                        } else {
                            console.log("Post project $newProject")
                            JsApi.newProject(newProject)
                        }
                    }.invokeOnCompletion { props.notifyOnSave() }
                    props.abortDialog()
                }
                +"Save"
            }
        }
    }
}
