package com.one_studio;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MidnightAssisitModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.midnight-assisit.config"))
                .setSavingRunnable(MidnightAssisitConfig.INSTANCE::save);

            var entryBuilder = builder.entryBuilder();
            var general = builder.getOrCreateCategory(Text.translatable("category.midnight-assisit.general"));

            // Global Enabled
            general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.midnight-assisit.global_enabled"), MidnightAssisitConfig.INSTANCE.getData().getGlobalEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(MidnightAssisitConfig.INSTANCE.getData()::setGlobalEnabled)
                .build());

            // Entities Category
            var entities = builder.getOrCreateCategory(Text.translatable("category.midnight-assisit.entities"));
            
            List<String> sortedEntities = new ArrayList<>(MidnightAssisitConfig.INSTANCE.getData().getEnabledEntities().keySet());
            Collections.sort(sortedEntities);
            
            for (String id : sortedEntities) {
                var entityType = MidnightAssisitConfig.INSTANCE.getEntityType(id);
                
                if (entityType == null) continue;
                
                var entityName = entityType.getName();
                boolean smartDefault = MidnightAssisitConfig.INSTANCE.isSmartDefault(entityType, id);

                entities.addEntry(entryBuilder.startBooleanToggle(entityName, MidnightAssisitConfig.INSTANCE.getData().getEnabledEntities().getOrDefault(id, smartDefault))
                    .setDefaultValue(smartDefault)
                    .setSaveConsumer(value -> MidnightAssisitConfig.INSTANCE.getData().getEnabledEntities().put(id, value))
                    .build());
            }

            return builder.build();
        };
    }
}
