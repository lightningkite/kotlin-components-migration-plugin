package com.ivieleague.mega.plugin

import org.jetbrains.kotlin.idea.refactoring.memberInfo.qualifiedClassNameForRendering
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.containingClass

/**
 * Created by josep on 4/26/2017.
 */
class ConversionTool {
    val nameMap = HashMap<String, String>()

    val KtClassOrObject.megaName: String get() {
        val name = this.qualifiedClassNameForRendering()
        return nameMap[name] ?: name
    }

    val KtFunction.megaName: String get() {
        val containingName = this.containingClass()?.megaName
        val baseName = if (containingName != null) (containingName + "." + (this.name ?: "constructor")) else (this.name ?: "constructor")
        val name = baseName + "-" + this.valueParameters.joinToString { it.typeReference?.text ?: "X" }
        return nameMap[name] ?: name
    }

    fun generateInterpretation(func: KtFunction): String {

        val rtr = func.receiverTypeReference
        val containingClass = func.containingClass()
        val callStart = if (rtr != null) retrieval("this", rtr.text) + "."
        else if (containingClass != null) retrieval("this", containingClass.qualifiedClassNameForRendering()) + "."
        else ""
        val parameters = func.valueParameters.joinToString(", ", "(", ")") { retrieval(it.name ?: "arg", it.typeReference?.fqName ?: "X") }

        return "functions[\"" + func.megaName + "\' = StandardFunction {\n" +
                "\t" + callStart + func.name + parameters + "\n" +
                "}"
    }

    fun retrieval(key: String, type: String) = "(it.execute(\"$key\") as $type)"

    val KtTypeReference.fqName: String? get() {
        val text = this.text
        val importDirective = this.containingKtFile.importDirectives.find {
            it.aliasName == text || it.importedFqName?.shortName()?.identifier == text
        }
        if (importDirective != null) {
            println(importDirective)
            return importDirective.importedFqName?.asString()
        }
        val declaration = this.containingKtFile.declarations.asSequence()
                .mapNotNull { it as? KtClassOrObject }
                .find { it.name == text }
        return declaration?.fqName?.asString()
    }
}