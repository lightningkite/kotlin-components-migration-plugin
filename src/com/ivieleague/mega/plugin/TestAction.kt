package com.ivieleague.mega.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtPsiFactory
import kotlin.reflect.KFunction
import groovyjarjarantlr.CodeGenerator
import com.intellij.testFramework.LightPlatformTestCase.getProject




/**
 * Test action
 * Created by josep on 4/24/2017.
 */
class TestAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        try {
            val project = event.getData(PlatformDataKeys.PROJECT)
            val type = getKtClassFromAction(event) ?: return
            println(type.docComment?.text)
            Messages.showMessageDialog(project, "The class currently selected is called ${type.name}", "Information", Messages.getInformationIcon())

            val factory = KtPsiFactory(event.project)
            val functions = type.declarations.filter { decl -> decl is KtFunction }
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
        } catch(e:Throwable){
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
