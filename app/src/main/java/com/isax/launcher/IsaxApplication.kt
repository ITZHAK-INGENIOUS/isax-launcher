package com.isax.launcher

import android.app.Application
import com.isax.launcher.core.IsaxPaths
import com.isax.launcher.core.Prefs
import com.isax.launcher.quest.QuestRepository
import com.isax.launcher.skills.SkillRegistry
import com.isax.launcher.skills.builtin.ClockSkill
import com.isax.launcher.skills.builtin.SystemPulseSkill

class IsaxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        IsaxPaths.ensure(this)
        QuestRepository.init(this)
        // Compétences embarquées (toujours disponibles)
        SkillRegistry.register(ClockSkill())
        SkillRegistry.register(SystemPulseSkill())
        // Compétences installées depuis /files/skills/ (packs JSON + bundles)
        SkillRegistry.loadInstalled(this)
    }
}
