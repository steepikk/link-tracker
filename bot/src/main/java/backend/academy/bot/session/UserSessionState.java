package backend.academy.bot.session;

public enum UserSessionState {
    WAITING_FOR_START, // Ждёт, пока начнётся программа, то есть пользователь введёт команду /start
    WAITING_FOR_COMMAND, // Ожидание команды (/help, /track, /list, /untrack)
    PROCESSING_COMMAND, // Обработка команд (не track)
    WAITING_FOR_LINK, // Ожидание ввода ссылки для /track (если ссылка не передана сразу)
    ASKING_FOR_TAG, // Запрос тега
    WAITING_FOR_TAG_INPUT, // Ожидание ввода тега
    WAITING_FOR_FILTER_INPUT, // Ожидание ввода фильтров
}
