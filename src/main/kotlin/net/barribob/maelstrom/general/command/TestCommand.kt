package net.barribob.maelstrom.general.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.format
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.synchronization.SuggestionProviders
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import java.util.*
import kotlin.system.measureNanoTime

class TestCommand() : CommandRegistrationCallback {
    private val notFoundException = DynamicCommandExceptionType { Component.literal("Test name not found") }

    private val tests = mutableMapOf<Identifier, (CommandSourceStack) -> Unit>()

    private val nameArgumentName = "name"

    init {
    }

    private val suggestions: SuggestionProvider<CommandSourceStack> =
        SuggestionProviders.register(
            Identifier.fromNamespaceAndPath(MaelstromMod.MODID, "test"),
            SuggestionProvider { _, builder ->
                SharedSuggestionProvider.filterResources(
                    tests.keys,
                    builder.remaining.lowercase(Locale.ROOT),
                    { it },
                    { builder.suggest(it.toString()) })
                return@SuggestionProvider builder.buildFuture()
            })

    fun addId(
        name: String,
        callback: (CommandSourceStack) -> Unit
    ) = tests.put(Identifier.parse(name.lowercase(Locale.ROOT)), callback)

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>, registryAccess: CommandBuildContext, environment: Commands.CommandSelection) {
        val commandName = "libtest"
        val timeArgumentName = "ticks"
        dispatcher.register(
            Commands.literal(commandName).then(
                Commands.argument(nameArgumentName, TestArgumentType(this))
                    .suggests(suggestions)
                    .executes { run(it) }
                    .then(
                        Commands.argument(timeArgumentName, IntegerArgumentType.integer(1))
                            .executes { run(it, IntegerArgumentType.getInteger(it, timeArgumentName)) }
                    )
            )
        )
    }

    private fun run(context: CommandContext<CommandSourceStack>, ticks: Int = 1): Int {
        val identifier = context.getArgument(nameArgumentName, Identifier::class.java)
        validate(identifier)
        var time = 0L
        val invoke = tests[identifier]
        
        val runTest: () -> Unit = {
            try {
                time += measureNanoTime { invoke?.invoke(context.source) }
            } catch (e: Exception) {
                context.source.sendSuccess({Component.literal(e.message ?: e.toString())}, false)
                e.printStackTrace()
            }
        }

        val eventScheduler = ModComponents.getWorldEventScheduler(context.source.level)
        eventScheduler.addEvent(TimedEvent(runTest, 0, ticks))
        eventScheduler.addEvent(TimedEvent({
            context.source.sendSuccess(
                {Component.literal("Test(s) ran using ${((time / ticks) * 1e-6).format(3)} ms of runtime")},
                false
            )
        }, ticks))
        

        return 1
    }

    fun validate(identifier: Identifier) {
        if (!tests.containsKey(identifier)) {
            throw notFoundException.create(identifier)
        }
    }
}