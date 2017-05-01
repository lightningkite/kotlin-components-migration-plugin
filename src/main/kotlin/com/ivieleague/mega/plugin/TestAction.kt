package com.ivieleague.mega.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtPsiFactory


/**
 * Test action
 * Created by josep on 4/24/2017.
 */
class TestAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        try {
            val project = event.getData(PlatformDataKeys.PROJECT)
            val type = getKtClassFromAction(event) ?: return
            handleType(event, project, type)
        } catch(e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun handleType(event: AnActionEvent, project: Project?, type: KtClass) {
        println(type.docComment?.text)
        Messages.showMessageDialog(project, "The class currently selected is called ${type.name}", "Information", Messages.getInformationIcon())

        val conversionTool = ConversionTool().apply {
            nameMap["kotlin.Byte"] = "mega.integer.signed.1"
            nameMap["kotlin.Short"] = "mega.integer.signed.2"
            nameMap["kotlin.Int"] = "mega.integer.signed.4"
            nameMap["kotlin.Long"] = "mega.integer.signed.8"
            nameMap["kotlin.Float"] = "mega.float.4"
            nameMap["kotlin.Double"] = "mega.float.8"
        }
        val factory = KtPsiFactory(event.project)
        val functions = type.functionSequence().toList()
        functions.forEach {
            println(conversionTool.generateInterpretation(it))
        }
        val body = type.getBody() ?: return
        println("Body found, writing")
        object : WriteCommandAction.Simple<Unit>(project, type.containingFile) {
            @Throws(Throwable::class)
            override fun run() {
                for (func in functions) {
                    body.addBefore(factory.createFunction("fun ${func.name}Plus() = ${func.name}()"), body.rBrace!!)
                }
            }
        }.execute()
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
