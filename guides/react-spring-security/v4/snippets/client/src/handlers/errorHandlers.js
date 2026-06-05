export const defaultErrorHandler = (error, handler) => {
    console.error(error);

    if(handler) {
        handler.call();
    }
};
