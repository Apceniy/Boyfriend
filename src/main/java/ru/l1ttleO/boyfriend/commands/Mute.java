package ru.l1ttleO.boyfriend.commands;

import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import ru.l1ttleO.boyfriend.Boyfriend;
import ru.l1ttleO.boyfriend.Duration;

public class Mute {
    public static final String usage = "`!mute <@упоминание или ID> [<продолжительность>] <причина>`";

    public void run(final MessageReceivedEvent event, final String[] args) {
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member muted;
        assert author != null;
        if (!author.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length == 0) {
            channel.sendMessage("Нету аргументов! " + usage).queue();
            return;
        }
        try {
            final String id = args[0].replaceAll("[^0-9]", "").replace("!", "").replace(">", "");
            muted = guild.retrieveMemberById(id).complete();
        } catch (final NumberFormatException e) {
            channel.sendMessage("Неправильно указан пользователь! " + usage).queue();
            return;
        }
        if (muted == null) {
            channel.sendMessage("Указан недействительный пользователь!").queue();
            return;
        }
        if (!author.canInteract(muted)) {
            channel.sendMessage("У тебя недостаточно прав для бана этого пользователя!").queue();
            return;
        }

        List<Role> roleList = guild.getRolesByName("заключённый", true);
        if (roleList.isEmpty())
            roleList = guild.getRolesByName("muted", true);
        if (roleList.isEmpty()) {
            channel.sendMessage("Не найдена роль мута!").queue();
            return;
        }
        final Role role = roleList.get(0);
        final int duration = Duration.getDurationMultiplied(args[1]);
        int startIndex = 1;
        String durationString;
        durationString = "всегда";
        if (duration != 0) {
            final String multiplier = Duration.getDurationMultiplier(args[1]);
            durationString = " " + args[1].replaceAll("[A-z]", "" + multiplier);
            startIndex = 2;
        }
        final String reason = StringUtils.join(args, ' ', startIndex, args.length);
        if (reason == null || reason.equals("")) {
            channel.sendMessage("Требуется указать причину!").queue();
            return;
        }
        Boyfriend.memberActions.muteMember(channel, role, author, muted, reason, duration, durationString);
    }
}