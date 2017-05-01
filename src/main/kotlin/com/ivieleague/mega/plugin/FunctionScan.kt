package com.ivieleague.mega.plugin

import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import kotlin.coroutines.experimental.buildSequence

/**
 * Scans for functions.
 * Created by josep on 4/26/2017.
 */
fun KtFile.functionSequence(): Sequence<KtFunction> = buildSequence {
    for (declaration in declarations) {
        when (declaration) {
            is KtClassOrObject -> yieldAll(declaration.functionSequence())
            is KtFunction -> yield(declaration as KtFunction)
        }
    }
}

fun KtClassOrObject.functionSequence(): Sequence<KtFunction> = buildSequence {
    for (declaration in declarations) {
        when (declaration) {
            is KtClassOrObject -> yieldAll(declaration.functionSequence())
            is KtFunction -> yield(declaration as KtFunction)
        }
    }
}