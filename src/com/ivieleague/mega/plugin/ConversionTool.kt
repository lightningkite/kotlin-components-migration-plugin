package com.ivieleague.mega.plugin

import org.jetbrains.kotlin.idea.refactoring.memberInfo.qualifiedClassNameForRendering
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
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
        val parameters = func.valueParameters.joinToString(", ", "(", ")") { retrieval(it.name ?: "arg", it.typeReference?.typeElement?.?: "X") }

        return "functions[\"" + func.megaName + "\' = StandardFunction {\n" +
                "\t" + callStart + func.name + parameters + "\n" +
                "}"
    }

    fun retrieval(key: String, type: String) = "(it.execute(\"$key\") as $type)"

    fun asdf(file: KtFile) {
        file.importList?.imports?.firstOrNull()?.importPath?.
    }
}