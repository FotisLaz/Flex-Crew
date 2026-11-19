import { useEffect, useState } from "react";
import { AxiosInstance } from "axios";
import { FaClock } from "react-icons/fa";
import { Record } from "../../types/Record";
import { getErrorMessage } from "../../api/getErrorMessage";

interface CheckInCardProps {
  api: AxiosInstance;
  employeeId: number;
  email: string;
}

const isToday = (date: Date) => date.toDateString() === new Date().toDateString();

const formatTime = (date: Date) =>
  date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

const CheckInCard = ({ api, employeeId, email }: CheckInCardProps) => {
  const [checkedInAt, setCheckedInAt] = useState<Date | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    api
      .get<Record[]>("/api/records/search", {
        params: { employeeEmail: email },
        signal: controller.signal,
      })
      .then(({ data }) => {
        const today = data.map((record) => new Date(record.startTime)).find(isToday);
        setCheckedInAt(today ?? null);
        setLoading(false);
      })
      .catch((err) => {
        if (controller.signal.aborted) return;
        setError(getErrorMessage(err, "Could not load today's check-in."));
        setLoading(false);
      });
    return () => controller.abort();
  }, [api, email]);

  const checkIn = async () => {
    setSubmitting(true);
    setError(null);
    try {
      // Only the employee is sent: the server sets the check-in time and schedule.
      const { data } = await api.post<Record>("/api/records", { employee: { id: employeeId } });
      setCheckedInAt(new Date(data.startTime));
    } catch (err) {
      setError(getErrorMessage(err, "Check-in failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="bg-white p-4 rounded-xl shadow-lg flex items-center space-x-4 border-l-4 border-yellow-FlexCrew">
      <FaClock size={32} className="text-yellow-FlexCrew" />
      <div>
        <p className="text-sm text-gray-500">Today's Check-in</p>
        {loading ? (
          <p className="text-lg font-semibold text-gray-800">Loading...</p>
        ) : checkedInAt ? (
          <p className="text-lg font-semibold text-gray-800">
            Checked in today at {formatTime(checkedInAt)}
          </p>
        ) : (
          <button
            type="button"
            onClick={checkIn}
            disabled={submitting}
            className="mt-1 px-4 py-2 bg-yellow-FlexCrew text-black font-semibold rounded-md hover:opacity-90 disabled:opacity-50"
          >
            {submitting ? "Checking in..." : "Check in"}
          </button>
        )}
        {error && (
          <p role="alert" className="mt-1 text-sm text-red-600">
            {error}
          </p>
        )}
      </div>
    </div>
  );
};

export default CheckInCard;
