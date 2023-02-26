package me.ordalca.nuzlocke.networking.proxies;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.ordalca.nuzlocke.ModFile;
import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;

public class PokemonHolder {
    public String species;
    public String name;
    public UUID pokemonID;
    private PokemonHolder() {}
    public PokemonHolder(Pokemon pokemon) {
        this.pokemonID = pokemon.getUUID();
        this.species = pokemon.getSpecies().getLocalizedName();
        this.name = pokemon.getDisplayName();
    }
    public boolean isNicknamed() {
        ModFile.LOGGER.debug("Checking "+name+" the "+species);
        return !(name.equalsIgnoreCase(species));
    }

    public CompoundNBT store() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("species", this.species);
        nbt.putString("name", this.name);
        nbt.putUUID("uuid",this.pokemonID);
        return nbt;
    }

    public static PokemonHolder create(CompoundNBT nbt) {
        PokemonHolder holder =  new PokemonHolder();
        holder.species = nbt.getString("species");
        holder.name = nbt.getString("name");
        holder.pokemonID = nbt.getUUID("uuid");
        return holder;
    }
}
