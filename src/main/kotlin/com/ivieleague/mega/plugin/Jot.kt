package com.ivieleague.mega.plugin

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*

interface ViewProvider<T> {
    fun getViewArguments(from:T):List<KtValueArgument>
    fun getViewCreation(from:T):ViewCreation
    fun getVariables(from:T):List<KtVariableDeclaration>
    fun getFunctions(from:T):List<KtFunction>
    fun getInitLogic(from:T):List<KtStatementExpression>
    fun getViews(from:T):List<KtVariableDeclaration>
    fun getViewBindings(from:T):List<ViewBinding>
}

class ViewBinding(
        val id:KtElement,
        val variable:KtVariableDeclaration
)

class ViewListenerBinding(
        val id:KtElement,
        val addOrSetListenerFunction:String,
        val block:KtElement
)

sealed class ViewCreation{
    class Inflation(val resourceReference:KtElement)
    class Manual(val expression:KtElement)
}