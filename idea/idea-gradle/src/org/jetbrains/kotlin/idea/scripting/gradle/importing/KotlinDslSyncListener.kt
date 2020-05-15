/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.scripting.gradle.importing

import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import org.jetbrains.kotlin.idea.core.script.ScriptDefinitionContributor
import org.jetbrains.kotlin.idea.framework.GRADLE_SYSTEM_ID
import org.jetbrains.kotlin.idea.scripting.gradle.GradleScriptDefinitionsContributor

class KotlinDslSyncListener : ExternalSystemTaskNotificationListenerAdapter() {
    override fun onStart(id: ExternalSystemTaskId, workingDir: String?) {
        if (id.type != ExternalSystemTaskType.RESOLVE_PROJECT) return
        if (id.projectSystemId != GRADLE_SYSTEM_ID) return

        gradleState.isSyncInProgress = true
    }

    override fun onEnd(id: ExternalSystemTaskId) {
        if (id.type != ExternalSystemTaskType.RESOLVE_PROJECT) return
        if (id.projectSystemId != GRADLE_SYSTEM_ID) return

        gradleState.isSyncInProgress = false

        val project = id.findProject() ?: return

        val gradleDefinitionsContributor =
            ScriptDefinitionContributor.find<GradleScriptDefinitionsContributor>(
                project
            )
        gradleDefinitionsContributor?.reloadIfNecessary()
    }

    companion object {
        internal val gradleState = GradleSyncState()
    }
}

// TODO: state should be stored by gradle build,
// now it is marked as complete after first gradle project was imported
internal class GradleSyncState {
    var isSyncInProgress: Boolean = false
}
