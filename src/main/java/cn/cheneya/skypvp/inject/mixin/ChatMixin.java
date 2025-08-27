package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.command.CommandManager;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatMixin {
    @Shadow
    @Final
    private TextFieldWidget textField;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    private ParseResults<CommandSource> parse;
    @Shadow public abstract void show(boolean narrateFirstSuggestion);

    @Shadow @Nullable private ChatInputSuggestor.@Nullable SuggestionWindow window;

    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false), cancellable = true)
    private void injectAutoCompletionB(CallbackInfo ci) {
        if (this.textField.getText().startsWith(CommandManager.Options.INSTANCE.getPrefix())) {
            this.pendingSuggestions = CommandManager.INSTANCE.autoComplete(this.textField.getText(), this.textField.getCursor());
            this.pendingSuggestions.thenRun(() -> {
                if(this.pendingSuggestions.isDone() && window == null) {
                    this.show(false);
                }
            });

            this.parse = null;

            ci.cancel();
        }
    }

}
