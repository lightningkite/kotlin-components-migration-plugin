package com.ivieleague.mega.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.psi.KtClass


/**
 * Test action
 * Created by josep on 4/24/2017.
 */
class TestAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        try {
            val type = getKtClassFromAction(event)
            println(type?.docComment?.text)
            Messages.showMessageDialog(project, "The class currently selected is called ${type?.name}", "Information", Messages.getInformationIcon())
        } catch(e: Throwable) {
            Messages.showMessageDialog(project, "It died.  ${e.message}", "Information", Messages.getInformationIcon())
            e.printStackTrace()
        }
    }

    fun getKtClassFromAction(event: AnActionEvent): KtClass? {
        val psiFile = event.getData(LangDataKeys.PSI_FILE)
        val editor = event.getData(PlatformDataKeys.EDITOR)

        if (psiFile == null || editor == null) {
            return null
        }

        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)

        return element?.let { getKtClassForElement(it) }
    }

    fun getKtClassForElement(psiElement: PsiElement): KtClass? {
        if (psiElement is KtLightElement<*, *>) {
            val origin = psiElement.kotlinOrigin
            if (origin != null) {
                return getKtClassForElement(origin)
            } else {
                return null
            }

        } else if (psiElement is KtClass && !psiElement.isEnum() &&
                !psiElement.isInterface() &&
                !psiElement.isAnnotation() &&
                !psiElement.isSealed()) {
            return psiElement

        } else {
            val parent = psiElement.parent
            if (parent == null) {
                return null
            } else {
                return getKtClassForElement(parent)
            }
        }
    }
}
