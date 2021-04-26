import { useNotifications, Message } from "./";

export const Messages = () => {
  const { notifications } = useNotifications();
  return notifications.length === 0 ? null : (
    <div
      className="fixed top-0 z-10 flex justify-center px-2 pt-4 left-1/2"
      style={{ transform: "translateX(-50%)" }}
    >
      <div className="flex flex-col items-center max-w-lg">
        {notifications.map((n) => (
          <Message key={n.id} notification={n} />
        ))}
      </div>
    </div>
  );
};
