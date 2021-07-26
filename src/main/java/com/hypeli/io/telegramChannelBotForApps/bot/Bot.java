package com.hypeli.io.telegramChannelBotForApps.bot;

import com.hypeli.io.telegramChannelBotForApps.model.Channel;
import com.hypeli.io.telegramChannelBotForApps.model.Message;
import com.hypeli.io.telegramChannelBotForApps.model.User;
import com.hypeli.io.telegramChannelBotForApps.service.ChannelService;
import com.hypeli.io.telegramChannelBotForApps.service.MessageService;
import com.hypeli.io.telegramChannelBotForApps.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
    Почти вся логика в этом классе находится из-за метода execute
    Его можно вызывать только в классе, наследуемом от TelegramLongPollingBot
    Но такой класс должен быть один
    Почти вся логика строится как раз вокруг метода execute именно в этом боте(проверка подписки на канал)
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.name}")
    private String name;

    @Value("${bot.admin}")
    private String adminId;

    @Value("${bot.chatId}")
    private String chatId;

    private final ChannelService channelService;
    private final MessageService messageService;
    private final UserService userService;
    private final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

    private SendPhoto sendPhoto;

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            User user = userService.findById(String.valueOf(update.getMessage().getFrom().getId()));
            if (update.getMessage().hasPhoto()){
                String chatId = String.valueOf(update.getMessage().getFrom().getId());

                if (adminId.equals(chatId)) {
                    sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chatId);

                    List<PhotoSize> photo = update.getMessage().getPhoto();
                    sendPhoto.setPhoto(new InputFile(photo.get(0).getFileId()));

                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Введите текст для рекламного сообщения");

                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException telegramApiException) {
                        telegramApiException.printStackTrace();
                    }

                    User admin = userService.findById(adminId);
                    admin.setPosition("rek");
                    userService.save(admin);

                    return;
                }

            }
            try {
                checkCommand(update);
            } catch (TelegramApiException telegramApiException) {
                telegramApiException.printStackTrace();
            }
        }
        else if (update.hasCallbackQuery()){
            try {
                checkJoined(update);
            } catch (TelegramApiException telegramApiException) {
                telegramApiException.printStackTrace();
            }
        }
        else if (update.hasChannelPost()){
            getChannelPost(update);
        }
    }


    public boolean check(String userId){
        Channel[] channelSavedInDatabase = channelService.findAll();

        GetChatMember getChatMember = new GetChatMember();
        List<String> statuses = new ArrayList<>();
        getChatMember.setUserId(Integer.valueOf(userId.trim()));

        ChatMember chatMember;

        if (channelSavedInDatabase.length == 0){
            return true;
        }

        for (Channel channel : channelSavedInDatabase){
            getChatMember.setChatId(channel.getId());
            try{
                chatMember = execute(getChatMember);
                statuses.add(chatMember.getStatus());
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        return !statuses.contains("left");
    }

    public void checkCommand(Update update) throws TelegramApiException {
        String mes = update.getMessage().getText();
        String userId = String.valueOf(update.getMessage().getFrom().getId());

        if (mes.split(" ").length > 1 && "/start".equals(mes.split(" ")[0])){

            User user = userService.findById(userId);
            user.setPosition("start " + mes.split(" ")[1]);
            userService.save(user);

            if (check(userId)) {

                User user1 = userService.findById(userId);
                int prioritet = 0;
                if (user1.getPosition().split(" ").length > 1
                        &&"/start".equals(user1.getPosition().split(" ")[1])){
                    prioritet = 1;
                }

                if (mes.split(" ").length > 1 &&"/start".equals(mes.split(" ")[0])){
                    prioritet = 2;
                }

                String messageId = " ";
                if (prioritet == 0){
                    return;
                }
                if (prioritet == 1) {
                    messageId = user1.getPosition().split(" ")[1];
                }
                if (prioritet == 2){
                    messageId = mes.split(" ")[1];
                }

                Message message = messageService.findById(messageId);

                ForwardMessage forwardMessage = new ForwardMessage();
                forwardMessage.setMessageId(Integer.valueOf(messageId));
                forwardMessage.setChatId(String.valueOf(update.getMessage().getFrom().getId()));
                forwardMessage.setFromChatId(message.getChatId());

                user.setPosition("exit");
                userService.save(user);

                execute(forwardMessage);
                return;
            }
            else execute(joinMenu(update));
            return;
        }
        // /add @id url
        else if (mes.split(" ").length > 1 && "/add".equals(mes.split(" ")[0])){
            SendMessage sendMessage = new SendMessage();
            if (adminId.equals(userId)) {
                String[] txt = mes.split(" ");
                Channel channel = new Channel();
                channel.setId(txt[1]);
                channel.setUrl(txt[2]);
                channelService.save(channel);

                sendMessage.setChatId(userId);

                if (channelService.findById(txt[1]) != null) {
                    sendMessage.setText("Канал добавлен");
                }
                else sendMessage.setText("Возникла ошибка при добавлении");

                execute(sendMessage);
                return;
            }
            sendMessage.setChatId(userId);
            sendMessage.setText("Вам недоступна команда!");
            return;
        }
        // /delete @id
        else if (mes.split(" ").length > 1 && "/delete".equals(mes.split(" ")[0])){
            SendMessage sendMessage = new SendMessage();
            if (adminId.equals(userId)) {
                String[] txt = mes.split(" ");
                channelService.delete(txt[1]);

                sendMessage.setChatId(userId);
                sendMessage.setText("Канал удален");

                execute(sendMessage);
                return;
            }
            sendMessage.setChatId(userId);
            sendMessage.setText("Вам недоступна команда!");
            return;
        }
        else if ("/rek".equalsIgnoreCase(mes)){
            SendMessage sendMessage = new SendMessage();
            if (adminId.equals(userId)) {
                sendMessage.setChatId(userId);
                sendMessage.setText("Отправьте фото БЕЗ ТЕКСТА");

                execute(sendMessage);
                return;
            }
            sendMessage.setChatId(userId);
            sendMessage.setText("Вам недоступна команда!");
            return;
        }

            if (adminId.equals(String.valueOf(update.getMessage().getFrom().getId()))){
                     User admin = userService.findById(adminId);
                if ("rek".equals(admin.getPosition())){
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(update.getMessage().getText());
                sendMessage.enableMarkdownV2(true);

                List<User> users = userService.findAll();
                users.forEach(x -> {
                    sendMessage.setChatId(x.getId());
                    sendPhoto.setChatId(x.getId());
                    try {
                        execute(sendPhoto);
                        execute(sendMessage);
                    } catch (TelegramApiException telegramApiException) {
                        telegramApiException.printStackTrace();
                    }
                });
                admin.setPosition("exit");
                userService.save(admin);

                sendPhoto = null;
            }
            }
            else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(userId);
                sendMessage.setText("Такой команды нет!");
                execute(sendMessage);
            }
    }

    public SendMessage joinMenu(Update update){
        String userId = "";
        if (update.hasMessage()) {
            userId = String.valueOf(update.getMessage().getFrom().getId());
        } else userId = String.valueOf(update.getCallbackQuery().getFrom().getId());
        Channel[] channels = channelService.findAll();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        StringBuilder sb = new StringBuilder();
        sb.append("Вам необходимо подписаться на ВСЕ каналы:\n");

        Arrays.stream(channels).forEach(c -> {
            sb.append("@").append(c.getId()).append("\n");
        });

        sendMessage.setText(sb.toString());

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton("Проверить");
        button.setCallbackData("checkJoin");
        row.add(button);
        rowList.add(row);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return sendMessage;
    }

    public void checkJoined(Update update) throws TelegramApiException {
        String userId = String.valueOf(update.getCallbackQuery().getFrom().getId());
        boolean joined  = check(userId);

        List<SendMessage> messages = new ArrayList<>();

        if (joined){

            String chatId = update.hasMessage()
                    ? String.valueOf(update.getMessage().getFrom().getId())
                    : String.valueOf(update.getCallbackQuery().getFrom().getId());

            User user = userService.findById(userId);
            String messageId = user.getPosition().split(" ")[1];
            Message message = messageService.findById(messageId);

            ForwardMessage forwardMessage = new ForwardMessage();
            forwardMessage.setMessageId(Integer.valueOf(messageId));
            forwardMessage.setChatId(chatId);
            forwardMessage.setFromChatId(message.getChatId());

            user.setPosition("exit");
            userService.save(user);

            execute(forwardMessage);
            return;
        }
        else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userId);
            sendMessage.enableHtml(true);
            sendMessage.setText("Вы подписались не на все каналы!");

            messages.add(sendMessage);
            messages.add(joinMenu(update));

            messages.forEach(x -> {
                try {
                    execute(x);
                } catch (TelegramApiException telegramApiException) {
                    telegramApiException.printStackTrace();
                }
            });
        }
    }

    public void getChannelPost(Update update){
        org.telegram.telegrambots.meta.api.objects.Message message = update.getChannelPost();
        Message msg = new Message();
        msg.setId(String.valueOf(message.getMessageId()));
        msg.setChatId(String.valueOf(message.getChatId()));
        log.info(" idMes: {} , idChat: {} , idFromMsg: {}", update.getChannelPost().getMessageId(), chatId, msg.getId());
        messageService.save(msg);
    }

}
