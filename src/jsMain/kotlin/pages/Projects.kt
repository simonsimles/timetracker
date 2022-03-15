package pages

import JsApi
import de.simles.timetracker.models.Project
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*
import scope

external interface ProjectsProps : RProps

external interface ProjectsState : RState {
    var projects: List<Project>?
    var selectedProject: Project?
    var newProjectName: String?
    var newProjectCategory: String?
}


class Projects(props: ProjectsProps) : RComponent<ProjectsProps, ProjectsState>(props) {
    init {
        state.apply { }
        loadProjects()
    }

    private fun loadProjects() {
        scope.launch {
            val retrievedProjects = JsApi.getProjects()
            console.log("Received projects")
            setState {
                projects = retrievedProjects
            }
        }
    }

    private fun resetEditDialog() {
        setState {
            selectedProject = null
            newProjectCategory = null
            newProjectName = null
        }
    }

    override fun RBuilder.render() {
        section(classes = "pt-md-5 pb-md-4 text-center") {
            div(classes = "container align-items-center") {
                div(classes = "row") {
                    div(classes = "col") {
                        table(classes = "table table-hover") {
                            thead {
                                tr {
                                    th {
                                        +"Project Name"
                                    }
                                    th {
                                        +"Category"
                                    }
                                }
                            }
                            tbody {
                                state.projects?.sortedBy { it.name }?.map {
                                    tr {
                                        td {
                                            +it.name
                                        }
                                        td {
                                            +it.category
                                        }
                                        attrs {
                                            onClickFunction = { _ -> setState { selectedProject = it } }
                                            if (state.selectedProject?.equals(it) == true) {
                                                classes = setOf("table-dark")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        button(classes = "btn btn-secondary", type = ButtonType.button) {
                            attrs { onClickFunction = { _ -> setState { selectedProject = Project("", "") } } }
                            +"New Project"
                        }
                    }
                    state.selectedProject?.let {
                        div(classes = "col justify-content-md-center") {
                            h3(classes = "mb-3") {
                                +"Edit project"
                                button {
                                    attrs {
                                        type = ButtonType.button
                                        classes = setOf("btn-close")
                                        onClickFunction = { _ -> resetEditDialog() }
                                    }
                                }
                            }
                            div(classes = "input-group mb-3") {
                                div(classes = "input-group-prepend") {
                                    div(classes = "input-group-text") { +"Project name" }
                                }
                                input(InputType.text, classes = "form-control") {
                                    attrs {
                                        defaultValue = it.name
                                        onChangeFunction = { event ->
                                            setState {
                                                newProjectName = (event.target as HTMLInputElement).value
                                            }
                                        }
                                    }
                                }
                            }
                            div(classes = "input-group mb-3") {
                                div(classes = "input-group-prepend") {
                                    div(classes = "input-group-text") { +"Category" }
                                }
                                input(InputType.text, classes = "form-control") {
                                    attrs {
                                        defaultValue = it.category
                                        onChangeFunction = { event ->
                                            setState {
                                                newProjectCategory = (event.target as HTMLInputElement).value
                                            }
                                        }
                                    }
                                }
                            }
                            button(classes = "btn btn-primary") {
                                attrs {
                                    type = ButtonType.button
                                    onClickFunction = { _ ->
                                        val projectName = state.newProjectName ?: state.selectedProject?.name
                                        val projectCategory =
                                            state.newProjectCategory ?: state.selectedProject?.category
                                        if (projectName == null || projectCategory == null) {
                                            console.log("$projectName or $projectCategory is null, cannot save")
                                        } else {
                                            val newProject = Project(projectName, projectCategory)
                                            val isUpdate = state.selectedProject?.name == projectName
                                            scope.launch {
                                                if (isUpdate) {
                                                    console.log("Patch project $newProject")
                                                    JsApi.updateProject(newProject)
                                                } else {
                                                    console.log("Post project $newProject")
                                                    JsApi.newProject(newProject)
                                                }
                                            }.invokeOnCompletion { loadProjects() }
                                        }
                                        resetEditDialog()
                                    }
                                }
                                +"Save"
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.projects(handler: ProjectsProps.() -> Unit): ReactElement {
    return child(Projects::class) {
        this.attrs(handler)
    }
}
