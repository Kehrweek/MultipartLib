package de.kehrweek.multipartlib.api.render;

import com.google.common.collect.ImmutableMap;
import de.kehrweek.multipartlib.api.part.Part;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public final class MPPartRenderers {

    private static final Map<Part, PartRendererFactory> FACTORIES = new HashMap<>();


    private MPPartRenderers() {

    }


    public static void register(Part part, PartRendererFactory factory) {
        FACTORIES.put(part, factory);
    }

    @ApiStatus.Internal
    public static Map<Part, PartRenderer> reload(PartRendererFactory.Context ctx) {
        final ImmutableMap.Builder<Part, PartRenderer> builder = ImmutableMap.builder();
        FACTORIES.forEach((p, f) -> {
            try {
                builder.put(p, f.create(ctx));
            } catch (Throwable t) {
                final CrashReport cr = new CrashReport("Failed to create PartRenderer for Part %s".formatted(p), t);
                p.populateCrashReport(cr.addElement("Part Details"));
                throw new CrashException(cr);
            }
        });
        return builder.build();
    }

}