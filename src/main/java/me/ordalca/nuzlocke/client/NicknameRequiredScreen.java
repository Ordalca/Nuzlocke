package me.ordalca.nuzlocke.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pixelmonmod.pixelmon.api.util.helpers.ResourceLocationHelper;
import com.pixelmonmod.pixelmon.api.util.helpers.TextHelper;
import com.pixelmonmod.pixelmon.client.gui.pokechecker.ButtonRename;
import com.pixelmonmod.pixelmon.client.gui.widgets.IndexedButton;
import com.pixelmonmod.pixelmon.client.gui.widgets.text.TransparentTextFieldWidget;
import me.ordalca.nuzlocke.ModFile;
import me.ordalca.nuzlocke.networking.proxies.PokemonHolder;
import net.minecraft.client.gui.screen.Screen;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class NicknameRequiredScreen extends Screen {
    private static final int RENAME_BUTTON_ID = 0;
    public static ResourceLocation renameBox;
    protected int xSize = 176;
    private final PokemonHolder pokemon;
    private TransparentTextFieldWidget textField;
    private Widget button;

    static {
        renameBox = ResourceLocationHelper.of("nuzlocke:textures/gui/forcerename.png");
    }

    public NicknameRequiredScreen(PokemonHolder pokemon) {
        super(StringTextComponent.EMPTY);
        this.pokemon = pokemon;
    }
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void init() {
        ModFile.LOGGER.debug("Nickname Screen");

        super.init();
        this.button = this.addButton(new ButtonRename(RENAME_BUTTON_ID, this.width / 2 - 25, this.height / 4 + 80, I18n.get("gui.renamePoke.renamebutton"), this::actionPerformed));
        String pokemonName = this.pokemon.name;

        if (this.minecraft != null) {
            this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        }
        this.textField = new TransparentTextFieldWidget(font, this.width / 2 - 62, this.height / 4 + 47, 140, 30, pokemonName);
        this.textField.setFocus(true);
        this.textField.setFilter((value) -> {
            for (String DENY_CHAR : TextHelper.DENY_CHARS) {
                if (value.contains(DENY_CHAR)) {
                    return false;
                }
            }
            return true;
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (this.textField != null) {
            this.textField.tick();
            String currentRename = this.textField.getValue().trim();
            boolean notNamed = (pokemon.species).equalsIgnoreCase(currentRename);
            if (this.button!= null) {
                this.button.active = !currentRename.isEmpty() && !notNamed;
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft != null) {
            this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        }
    }

    protected void actionPerformed(Button button) {
        if (button instanceof IndexedButton) {
            pokemon.name = this.textField.getValue();
            ClientNicknameHandler.reportNickname(pokemon);
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (this.textField.canConsumeInput()) {
            if (key == 257) {
                this.actionPerformed((Button) this.button);
                return true;
            }
            return this.textField.keyPressed(key, scanCode, modifiers);
        } else {
            return super.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (this.textField.canConsumeInput()) {
            String previousValue = this.textField.getValue();
            this.textField.charTyped(typedChar, keyCode);
            if (this.textField.getValue(true).length() > 16) {
                this.textField.setValue(previousValue);
            }
            return true;
        } else {
            return super.charTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.textField.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.normal3f(0.0F, -1.0F, 0.0F);
        if (this.minecraft != null) {
            this.minecraft.getTextureManager().bind(NicknameRequiredScreen.renameBox);
            this.blit(matrix, (this.width - this.xSize) / 2 - 40, this.height / 4, 0, 0, 256, 114);
            drawCenteredString(matrix, this.minecraft.font, "Rename " + this.pokemon.name, this.width / 2, this.height / 4 - 60 + 80, 16777215);
            this.textField.render(matrix, mouseX, mouseY, partialTicks);
            this.button.render(matrix, mouseX, mouseY, partialTicks);
        }
    }
}